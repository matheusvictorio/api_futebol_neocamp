package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesRetrospectDTO;
import com.neocamp.api_futebol.dtos.response.OppRetrospectDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchValidationsService matchValidationsService;
    @Autowired
    private ClubRepository clubRepository;

    public MatchesResponseDTO createMatch(@Valid MatchesRequestDTO matchesRequestDTO) {
        Club homeClub = matchValidationsService.findClubOrThrow(matchesRequestDTO.homeClubId());
        Club awayClub =  matchValidationsService.findClubOrThrow(matchesRequestDTO.awayClubId());
        Stadium stadium = matchValidationsService.findStadiumOrThrow(matchesRequestDTO.stadiumId());

        matchValidationsService.validateNotSameClubs(homeClub, awayClub);
        matchValidationsService.validateClubsActive(homeClub, awayClub);
        matchValidationsService.validateDateAfterFoundation(matchesRequestDTO.matchDateTime(), homeClub, awayClub);
        matchValidationsService.validateNoNearMatches(homeClub, awayClub, matchesRequestDTO.matchDateTime());
        matchValidationsService.validateStadiumAvailable(stadium, matchesRequestDTO.matchDateTime());

        Match match = new Match(homeClub, awayClub, stadium, matchesRequestDTO.matchDateTime(), matchesRequestDTO.homeGoals(), matchesRequestDTO.awayGoals());
        matchRepository.save(match);

        String result = matchValidationsService.formatResult(match);
        String winner = matchValidationsService.determineWinner(match);

        return new MatchesResponseDTO(
                match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
                );
    }

    public MatchesResponseDTO updateMatch(Long id, MatchesRequestDTO matchesRequestDTO) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada!"));

        Club homeClub = matchValidationsService.findClubOrThrow(matchesRequestDTO.homeClubId());
        Club awayClub =  matchValidationsService.findClubOrThrow(matchesRequestDTO.awayClubId());
        Stadium stadium = matchValidationsService.findStadiumOrThrow(matchesRequestDTO.stadiumId());

        matchValidationsService.validateNotSameClubs(homeClub, awayClub);
        matchValidationsService.validateClubsActive(homeClub, awayClub);
        matchValidationsService.validateDateAfterFoundation(matchesRequestDTO.matchDateTime(), homeClub, awayClub);
        matchValidationsService.validateNoNearMatches(homeClub, awayClub, matchesRequestDTO.matchDateTime());
        matchValidationsService.validateStadiumAvailable(stadium, matchesRequestDTO.matchDateTime());

        match.setHomeClub(homeClub);
        match.setAwayClub(awayClub);
        match.setStadium(stadium);

        matchRepository.save(match);

        String result = matchValidationsService.formatResult(match);
        String winner = matchValidationsService.determineWinner(match);

        return new MatchesResponseDTO(match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
        );
    }

    public void deleteClub(Long id) {
        matchRepository.deleteById(id);
    }

    public MatchesResponseDTO findById(Long id) {
        var match = matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada!"));

        String result = matchValidationsService.formatResult(match);
        String winner = matchValidationsService.determineWinner(match);

        return new MatchesResponseDTO(
                match.getId(),
                match.getHomeClub().getName(),
                match.getAwayClub().getName(),
                match.getStadium().getName(),
                match.getMatchDateTime(),
                result,
                winner
        );
    }

    public Page<MatchesResponseDTO> searchMatches(Long clubId, Long stadiumId, Pageable pageable) {
        Page<Match> matches = matchRepository.findWithFilters(clubId, stadiumId, pageable);
        return matches.map(m -> {
            String result = matchValidationsService.formatResult(m);
            String winner = matchValidationsService.determineWinner(m);
            return new MatchesResponseDTO(m, result, winner);
        });
    }

    public MatchesRetrospectDTO getClubRetrospective(Long id) {
        clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clube não encontrado!"));

        List<Match> matches = matchRepository.findAllMatchesForClub(id);

        int victories = 0, draws = 0, defeats = 0, goalsFor = 0, goalsAgainst = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomeClub().getId().equals(id);

            // se for o time de casa pega os gols do time de casa, se for de fora pega os gols do time de fora
            int clubGoals = isHome ? match.getHomeGoals() : match.getAwayGoals();
            int oppGoals = isHome ? match.getAwayGoals() : match.getHomeGoals();

            goalsFor += clubGoals;
            goalsAgainst += oppGoals;

            if(clubGoals > oppGoals){
                victories++;
            } else if(oppGoals == clubGoals){
                draws++;
            } else defeats++;
        }
        return new MatchesRetrospectDTO(victories, draws, defeats, goalsFor, goalsAgainst);
    }

    public List<OppRetrospectDTO> getOppRetrospects(Long id) {
        clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clube não encontrado!"));

        List<OppRetrospectDTO> stats = matchRepository.findOppsStats(id);
        return stats;
    }

    public OppRetrospectDTO getOneOppRestrospect(Long id, Long oppId) {
        clubRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clube não encontrado!"));

        clubRepository.findById(oppId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Adversário não encontrado!"));

        String oppName = clubRepository.findById(oppId).get().getName();

        List<Match> matches = matchRepository.findAllMatchesBetweenClubs(id, oppId);

        int victories = 0, draws = 0, defeats = 0,  goalsFor = 0, goalsAgainst = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomeClub().getId().equals(id);

            var clubGoals = isHome? match.getHomeGoals() : match.getAwayGoals();
            var awayGoals = isHome? match.getAwayGoals() : match.getHomeGoals();

            goalsFor += clubGoals;
            goalsAgainst += awayGoals;

            if(clubGoals > awayGoals){
                victories++;
            } else if(awayGoals == clubGoals){
                draws++;
            }  else defeats++;
        }

        return new OppRetrospectDTO(oppId, oppName, victories, draws, defeats, goalsFor, goalsAgainst);
    }
}
