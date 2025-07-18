package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.*;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.NotFoundException;
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

import java.util.Comparator;
import java.util.List;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchValidationsService matchValidationsService;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private StadiumRepository stadiumRepository;

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
                .orElseThrow(() -> new NotFoundException("Partida não encontrada!"));

        Club homeClub = matchValidationsService.findClubOrThrow(matchesRequestDTO.homeClubId());
        Club awayClub =  matchValidationsService.findClubOrThrow(matchesRequestDTO.awayClubId());
        Stadium stadium = matchValidationsService.findStadiumOrThrow(matchesRequestDTO.stadiumId());

        matchValidationsService.validateNotSameClubs(homeClub, awayClub);
        matchValidationsService.validateClubsActive(homeClub, awayClub);
        matchValidationsService.validateDateAfterFoundation(matchesRequestDTO.matchDateTime(), homeClub, awayClub);
        matchValidationsService.validateNoNearMatches(homeClub, awayClub, matchesRequestDTO.matchDateTime(), id);
        matchValidationsService.validateStadiumAvailable(stadium, matchesRequestDTO.matchDateTime(), id);

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

    public void deleteMatch(Long id) {
        if (!matchRepository.existsById(id)) {
            throw new NotFoundException("Partida não encontrada!");
        }
        matchRepository.deleteById(id);
    }

    public MatchesResponseDTO findById(Long id) {
        var match = matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partida não encontrada!"));

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

    public Page<MatchesResponseDTO> searchMatches(Long clubId, Long stadiumId, Boolean routs, String side, Pageable pageable) {
        if (clubId != null && !clubRepository.existsById(clubId)) {
            throw new NotFoundException("Clube não encontrado!");
        }
        if (stadiumId != null && !stadiumRepository.existsById(stadiumId)) {
            throw new NotFoundException("Estádio não encontrado!");
        }
        if (side != null && !side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }
        Page<Match> matches = matchRepository.findWithFilters(clubId, stadiumId, routs, side, pageable);
        return matches.map(m -> {
            String result = matchValidationsService.formatResult(m);
            String winner = matchValidationsService.determineWinner(m);
            return new MatchesResponseDTO(m, result, winner);
        });
    }

    public MatchesRetrospectDTO getClubRetrospective(Long id, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado!"));

        List<Match> matches;

        if (side != null &&!side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }

        if ("casa".equalsIgnoreCase(side)) {
            matches = matchRepository.findAllHomeMatchesForClub(id);
        } else if ("fora".equalsIgnoreCase(side)) {
            matches = matchRepository.findAllAwayMatchesForClub(id);
        } else {
            matches = matchRepository.findAllMatchesForClub(id);
        }

        String clubName = clubRepository.findById(id).get().getName();

        int matchesQuantity = 0, victories = 0, draws = 0, defeats = 0, goalsFor = 0, goalsAgainst = 0;

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

            matchesQuantity++;
        }
        return new MatchesRetrospectDTO(clubName, matchesQuantity, victories, draws, defeats, goalsFor, goalsAgainst);
    }

    public List<OppRetrospectDTO> getOppRetrospects(Long id, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado!"));

        if (side != null &&!side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }
        List<OppRetrospectDTO> stats = matchRepository.findOppsStats(id, side);
        return stats;
    }

    public OppRetrospectDTO getOneOppRestrospect(Long id, Long oppId, String side) {
        clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado!"));

        clubRepository.findById(oppId)
                .orElseThrow(() -> new NotFoundException("Adversário não encontrado!"));


        String oppName = clubRepository.findById(oppId).get().getName();
        if (side != null &&!side.equalsIgnoreCase("casa") && !side.equalsIgnoreCase("fora")) {
            throw new BadRequestException("Lado inválido!");
        }
        List<Match> matches = matchRepository.findAllMatchesBetweenClubs(id, oppId, side);

        Long matchesQuantity = 0L, victories = 0L, draws = 0L, defeats = 0L,  goalsFor = 0L, goalsAgainst = 0L;

        for (Match match : matches) {
            boolean isHome = match.getHomeClub().getId().equals(id);

            int clubGoals = isHome? match.getHomeGoals() : match.getAwayGoals();
            int awayGoals = isHome? match.getAwayGoals() : match.getHomeGoals();

            goalsFor += clubGoals;
            goalsAgainst += awayGoals;

            if(clubGoals > awayGoals){
                victories++;
            } else if(awayGoals == clubGoals){
                draws++;
            }  else defeats++;

            matchesQuantity++;
        }

        return new OppRetrospectDTO(oppId, oppName, matchesQuantity, victories, draws, defeats, goalsFor, goalsAgainst);
    }

    public List<ClubRankingDTO> rankClubsByFilter(String filter) {
        List<ClubRankingDTO> ranking = matchRepository.findClubRanking();

        return switch(filter.toLowerCase()){
            case "pontos" -> ranking.stream()
                    .filter(r -> r.points() != null && r.points() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::points).reversed())
                    .toList();
            case "gols"  -> ranking.stream()
                    .filter(r -> r.goals() != null && r.goals() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::goals).reversed())
                    .toList();
            case "vitorias" ->  ranking.stream()
                    .filter(r -> r.victories() != null && r.victories() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::victories).reversed())
                    .toList();
            case "partidas" -> ranking.stream()
                    .filter(r -> r.matches() != null && r.matches() > 0)
                    .sorted(Comparator.comparing(ClubRankingDTO::matches).reversed())
                    .toList();
            default -> throw new BadRequestException("Filtro inválido!");
        };
    }
}
