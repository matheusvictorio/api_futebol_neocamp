package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.ConflictException;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class MatchValidationsService {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private StadiumRepository stadiumRepository;

    public void validateNotSameClubs(Club home, Club away){
        if(home.getId().equals(away.getId())){
            throw new BadRequestException("Clubes não podem ser iguais!");
        }
    }
    public void validateClubsActive(Club home, Club away){
        if(!home.getActive() || !away.getActive()){
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

    public void validateStadiumAvailable(Stadium stadium, LocalDateTime matchDateTime){
        if(!matchRepository.findByStadiumAndDay(stadium.getId(), matchDateTime).isEmpty()){
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
