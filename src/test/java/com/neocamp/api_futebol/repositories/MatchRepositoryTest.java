package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.entities.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MatchRepositoryTest {
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private StadiumRepository stadiumRepository;

    private Club createClub(String name, State state) {
        Club club = new Club(name, state, java.time.LocalDate.now());
        club.setActive(true);
        return clubRepository.save(club);
    }

    private Stadium createStadium(String name) {
        Stadium stadium = new Stadium();
        stadium.setName(name);
        stadium.setActive(true);
        return stadiumRepository.save(stadium);
    }

    private Match createMatch(Club home, Club away, Stadium stadium, LocalDateTime dateTime, int homeGoals, int awayGoals) {
        Match match = new Match();
        match.setHomeClub(home);
        match.setAwayClub(away);
        match.setStadium(stadium);
        match.setMatchDateTime(dateTime);
        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);
        return matchRepository.save(match);
    }

    @Test
    @DisplayName("should find matches with all filters")
    void findWithFilters_allFilters() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 4, 1); // rout
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(home.getId(), stadium.getId(), true, "casa", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find matches by clubId only")
    void findWithFilters_clubIdOnly() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 2, 1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(home.getId(), null, null, null, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find matches by stadiumId only")
    void findWithFilters_stadiumIdOnly() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 2, 1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(null, stadium.getId(), null, null, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find rout matches only")
    void findWithFilters_routsOnly() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 5, 1); // rout
        createMatch(home, away, stadium, LocalDateTime.now(), 2, 1); // not rout
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(null, null, true, null, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find matches by side casa")
    void findWithFilters_sideCasa() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 1, 0);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(home.getId(), null, null, "casa", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find matches by side fora")
    void findWithFilters_sideFora() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 1, 2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(away.getId(), null, null, "fora", pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should return all matches when all filters are null")
    void findWithFilters_allNull() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 1, 1);
        createMatch(away, home, stadium, LocalDateTime.now(), 2, 2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(null, null, null, null, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return no matches for impossible filter")
    void findWithFilters_noResults() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 1, 1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> result = matchRepository.findWithFilters(999L, 999L, true, "casa", pageable);
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("should find matches near date for club")
    void findMatchesNearDateForClub() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        LocalDateTime date = LocalDateTime.of(2025, 7, 15, 16, 0);
        createMatch(home, away, stadium, date.minusHours(24), 1, 1);
        createMatch(home, away, stadium, date.plusHours(24), 2, 2);
        List<Match> result = matchRepository.findMatchesNearDateForClub(home.getId(), date);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("should find all matches for club")
    void findAllMatchesForClub() {
        Club home = createClub("Flamengo", State.RJ);
        Club away = createClub("Vasco", State.RJ);
        Stadium stadium = createStadium("Maracana");
        createMatch(home, away, stadium, LocalDateTime.now(), 1, 1);
        createMatch(away, home, stadium, LocalDateTime.now(), 2, 2);
        List<Match> result = matchRepository.findAllMatchesForClub(home.getId());
        assertEquals(2, result.size());
    }
}

