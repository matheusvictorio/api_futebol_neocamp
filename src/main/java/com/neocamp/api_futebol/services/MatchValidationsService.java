package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.ConflictException;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MatchValidationsService {

    private final MatchRepository matchRepository;

    private final ClubRepository clubRepository;

    private final StadiumRepository stadiumRepository;

    public MatchValidationsService(MatchRepository matchRepository, ClubRepository clubRepository,
                                   StadiumRepository stadiumRepository) {
        this.matchRepository = matchRepository;
        this.clubRepository = clubRepository;
        this.stadiumRepository = stadiumRepository;
    }

    public void validateNotSameClubs(Club home, Club away){
        if(home.getId().equals(away.getId())){
            throw new BadRequestException("Clubes não podem ser iguais!");
        }
    }
    public void validateClubsActive(Club home, Club away){
        if (!Boolean.TRUE.equals(home.getActive()) || !Boolean.TRUE.equals(away.getActive())) {
            throw new ConflictException("Clube inativo!");
        }
    }

    public void validateDateAfterFoundation(LocalDateTime matchDateTime, Club home, Club away){
        if(matchDateTime.toLocalDate().isBefore(home.getCreatedAt())
        || matchDateTime.toLocalDate().isBefore(away.getCreatedAt())){
            throw new ConflictException("Partida não pode ser criada antes da fundação de algum dos clubes!");
        }
    }

    public void validateNoNearMatches(Club home, Club away, LocalDateTime matchDateTime){
        if(!matchRepository.findMatchesNearDateForClub(home.getId(), matchDateTime).isEmpty()
            || !matchRepository.findMatchesNearDateForClub(away.getId(), matchDateTime).isEmpty()){
            throw new ConflictException("Clubes possuem partidas próximas!");
        }
    }


    public void validateNoNearMatches(Club home, Club away, LocalDateTime matchDateTime, Long matchId){
        if(!matchRepository.findMatchesNearDateForClubIgnoringMatch(home.getId(), matchDateTime, matchId).isEmpty()
                || !matchRepository.findMatchesNearDateForClubIgnoringMatch(away.getId(), matchDateTime, matchId).isEmpty()){
            throw new ConflictException("Clubes possuem partidas próximas!");
        }
    }

    public void validateStadiumAvailable(Stadium stadium, LocalDateTime matchDateTime){
        if(!matchRepository.findByStadiumAndDay(stadium.getId(), matchDateTime).isEmpty()){
            throw new ConflictException("Estádio já tem partida no mesmo dia.");
        }
    }

    public void validateStadiumAvailable(Stadium stadium, LocalDateTime matchDateTime, Long matchId){
        if(!matchRepository.findByStadiumAndDayIgnoringMatch(stadium.getId(), matchDateTime, matchId).isEmpty()){
            throw new ConflictException("Estádio já tem partida no mesmo dia.");
        }
    }



    public Club findClubOrThrow(Long clubId){
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new BadRequestException("Clube não encontrado!"));
    }

    public Stadium findStadiumOrThrow(Long stadiumId){
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new BadRequestException("Estádio não encontrado!"));
    }

    public String determineWinner(Match match) {
        if(match.getHomeGoals() > match.getAwayGoals()){
            return match.getHomeClub().getName();
        } else if(match.getAwayGoals() > match.getHomeGoals()){
            return match.getAwayClub().getName();
        } else {
            return "Empate";
        }
    }

    public String formatResult(Match match){
        return match.getHomeGoals() + " x " +  match.getAwayGoals();
    }
}
