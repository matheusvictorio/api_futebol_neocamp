package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.ConflictException;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchValidationsServiceTest {
    @Mock
    MatchRepository matchRepository;
    @Mock
    ClubRepository clubRepository;
    @Mock
    StadiumRepository stadiumRepository;

    MatchValidationsService validations;

    Club club1, club2;
    Stadium stadium1;
    LocalDateTime matchDate = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        validations = new MatchValidationsService(matchRepository, clubRepository, stadiumRepository);

        club1 = new Club();
        club1.setId(1L);
        club1.setName("Club 1");
        club1.setActive(true);
        club1.setCreatedAt(LocalDate.now().minusYears(10));

        club2 = new Club();
        club2.setId(2L);
        club2.setName("Club 2");
        club2.setActive(true);
        club2.setCreatedAt(LocalDate.now().minusYears(10));

        stadium1 = new Stadium();
        stadium1.setId(1L);
        stadium1.setName("S1");
    }

    @Test
    @DisplayName("Should validate not same clubs")
    void validateNotSameClubsTest(){
        assertDoesNotThrow(() -> validations.validateNotSameClubs(club1, club2));
    }

    @Test
    @DisplayName("Should validate are the same clubs")
    void validateAreTheSameClubsTestCase2(){
        BadRequestException ex = assertThrows(BadRequestException.class, () -> validations.validateNotSameClubs(club1, club1));

        assertEquals("Clubes não podem ser iguais!", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate clubs active")
    void validateClubsActiveTest(){
        assertDoesNotThrow(() -> validations.validateClubsActive(club1, club2));
    }

    @Test
    @DisplayName("Should validate clubs inactive")
    void validateClubsInactiveTestCase2(){
        club1.setActive(false);
        ConflictException ex = assertThrows(ConflictException.class, () -> validations.validateClubsActive(club1, club2));

        assertEquals("Clube inativo!", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate date after foundation")
    void validateDateAfterFoundationTest(){
        assertDoesNotThrow(() -> validations.validateDateAfterFoundation(matchDate, club1, club2));
    }

    @Test
    @DisplayName("Should validate date before foundation")
    void validateDateBeforeFoundationTestCase2(){
        matchDate = matchDate.minusYears(11);
        ConflictException ex = assertThrows(ConflictException.class, () -> validations.validateDateAfterFoundation(matchDate, club1, club2));

        assertEquals("Partida não pode ser criada antes da fundação de algum dos clubes!", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate no near matches")
    void validateNoNearMatchesTest(){
        assertDoesNotThrow(() -> validations.validateNoNearMatches(club1, club2, matchDate));
    }

    @Test
    @DisplayName("Should validate near matches")
    void validateNoNearMatchesTestCase2(){
        Match match1 = new Match(club1, club2, stadium1, matchDate, 3, 1);
        when(matchRepository.findMatchesNearDateForClub(club1.getId(), matchDate)).thenReturn(List.of(match1));

        ConflictException ex = assertThrows(ConflictException.class, () -> validations.validateNoNearMatches(club1, club2, matchDate));

        assertEquals("Clubes possuem partidas próximas!", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate Stadium is available")
    void validateStadiumAvailableTest(){
        assertDoesNotThrow(() -> validations.validateStadiumAvailable(stadium1, matchDate));
    }

    @Test
    @DisplayName("Should validate Stadium is not available")
    void validateStadiumAvailableTestCase2(){
        Match match1 = new Match(club1, club2, stadium1, matchDate, 3, 1);
        when(matchRepository.findByStadiumAndDay(stadium1.getId(), matchDate)).thenReturn(List.of(match1));

        ConflictException ex = assertThrows(ConflictException.class, () -> validations.validateStadiumAvailable(stadium1, matchDate));

        assertEquals("Estádio já tem partida no mesmo dia.", ex.getMessage());
    }
}
