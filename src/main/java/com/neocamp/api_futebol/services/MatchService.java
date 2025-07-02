package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private MatchValidationsService matchValidationsService;

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
}
