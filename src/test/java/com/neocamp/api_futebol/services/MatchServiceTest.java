package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubRankingDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesRetrospectDTO;
import com.neocamp.api_futebol.dtos.response.OppRetrospectDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchValidationsService validations;
    @Mock
    private StadiumRepository stadiumRepository;
    @InjectMocks
    private MatchService matchService;

    private Club club1;
    private Club club2;
    private ClubRankingDTO clubRankingDTO1;
    private ClubRankingDTO clubRankingDTO2;
    private Stadium stadium1;

    @BeforeEach
    void setUp() {

        club1 = new Club();
        club1.setId(1L);
        club1.setName("Club 1");
        club1.setCreatedAt(LocalDate.now().minusYears(10));
        club2 = new Club();
        club2.setId(2L);
        club2.setName("Club 2");
        club2.setCreatedAt(LocalDate.now().minusYears(10));
        stadium1 = new Stadium();
        stadium1.setName("Estádio Central");
        stadium1.setId(1L);
        clubRankingDTO1 = new ClubRankingDTO(1L, club1.getName(), 6L, 5L, 2L, 3L);
        clubRankingDTO2 = new ClubRankingDTO(2L, club2.getName(), 2L, 1L, 1L, 3L);
    }

    @Test
    @DisplayName("should return the ranking by points")
    void getRanking() {
        when(matchRepository.findClubRanking()).thenReturn(Arrays.asList(clubRankingDTO1, clubRankingDTO2));

        List<ClubRankingDTO> ranking = matchService.rankClubsByFilter("pontos");
        assertFalse(ranking.isEmpty());
        assertEquals(6L, ranking.get(0).points());
        assertTrue(ranking.get(0).points() >= ranking.get(1).points());
    }

    @Test
    @DisplayName("should return the ranking by goals")
    void getRankingCase2() {
        when(matchRepository.findClubRanking()).thenReturn(Arrays.asList(clubRankingDTO1, clubRankingDTO2));
        List<ClubRankingDTO> ranking = matchService.rankClubsByFilter("gols");
        assertFalse(ranking.isEmpty());
        assertEquals(5L, ranking.get(0).goals());
        assertTrue(ranking.get(0).goals() >= ranking.get(1).goals());
    }

    @Test
    @DisplayName("should return the ranking by victories")
    void getRankingCase3() {
        when(matchRepository.findClubRanking()).thenReturn(Arrays.asList(clubRankingDTO1, clubRankingDTO2));
        List<ClubRankingDTO> ranking = matchService.rankClubsByFilter("vitorias");
        assertFalse(ranking.isEmpty());
        assertEquals(2L, ranking.get(0).victories());
        assertTrue(ranking.get(0).victories() >= ranking.get(1).victories());
    }

    @Test
    @DisplayName("should return the ranking by matches")
    void getRankingCase4() {
        when(matchRepository.findClubRanking()).thenReturn(Arrays.asList(clubRankingDTO1, clubRankingDTO2));
        List<ClubRankingDTO> ranking = matchService.rankClubsByFilter("partidas");
        assertFalse(ranking.isEmpty());
        assertEquals(3L, ranking.get(0).matches());
        assertTrue(ranking.get(0).matches() >= ranking.get(1).matches());
    }

    @Test
    @DisplayName("should return 400 when filter is invalid")
    void getRankingCase5() {
        when(matchRepository.findClubRanking()).thenReturn(Arrays.asList(clubRankingDTO1, clubRankingDTO2));
        assertThrows(BadRequestException.class, () -> matchService.rankClubsByFilter("invalid"));
    }


    @Test
    void testFindByIdSuccess() {

        Match match = new Match();
        match.setId(1L);
        match.setHomeClub(club1);
        match.setAwayClub(club2);
        match.setHomeGoals(2);
        match.setAwayGoals(1);
        match.setStadium(stadium1);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(validations.formatResult(match)).thenReturn("2x1");
        when(validations.determineWinner(match)).thenReturn("Club 1");

        var response = matchService.findById(1L);

        assertNotNull(response);
        assertEquals("2x1", response.result());
        assertEquals("Club 1", response.winner());
    }

    @Test
    void testFindByIdNotFound() {
        when(matchRepository.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThrows(com.neocamp.api_futebol.exception.NotFoundException.class, () -> matchService.findById(99L));
    }

    @Test
    void testDeleteMatchSuccess() {
        when(matchRepository.existsById(1L)).thenReturn(true);
        doNothing().when(matchRepository).deleteById(1L);
        matchService.deleteMatch(1L);
        verify(matchRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteMatchNotFound() {
        when(matchRepository.existsById(99L)).thenReturn(false);
        assertThrows(com.neocamp.api_futebol.exception.NotFoundException.class, () -> matchService.deleteMatch(99L));
    }


    @Test
    @DisplayName("should create a match with success")
    void CreateMatchCase1() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2,1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        when(validations.formatResult(any(Match.class))).thenReturn("2x1");
        when(validations.determineWinner(any(Match.class))).thenReturn("Club 1");

        MatchesResponseDTO response = matchService.createMatch(dto);

        verify(validations).validateNotSameClubs(club1, club2);
        verify(validations).validateClubsActive(club1, club2);
        verify(validations).validateDateAfterFoundation(dto.matchDateTime(), club1, club2);
        verify(validations).validateNoNearMatches(club1, club2, dto.matchDateTime());
        verify(validations).validateStadiumAvailable(stadium1, dto.matchDateTime());

        assertNotNull(response);
        assertEquals("2x1", response.result());
        assertEquals("Club 1", response.winner());
    }

    @Test
    @DisplayName("should throw exception when home and away clubs are the same")
    void CreateMatchCase2() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 1L, 1L, LocalDateTime.now(), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Clubes não podem ser iguais!"))
                .when(validations).validateNotSameClubs(club1, club1);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Clubes não podem ser iguais!", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when home club is inactive")
    void CreateMatchCase3() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now(), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        club1.setActive(false);

        doThrow(new BadRequestException("Clube inativo!"))
                .when(validations).validateClubsActive(club1, club2);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Clube inativo!", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when date is before home club foundation")
    void CreateMatchCase4() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusYears(11), 2, 1);


        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Partida não pode ser criada antes da fundação de algum dos clubes!"))
                .when(validations).validateDateAfterFoundation(dto.matchDateTime(), club1, club2);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Partida não pode ser criada antes da fundação de algum dos clubes!", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when match date is near another match date")
    void CreateMatchCase5() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Clubes possuem partidas próximas!"))
                .when(validations).validateNoNearMatches(club1, club2, dto.matchDateTime());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Clubes possuem partidas próximas!", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when stadium is not available")
    void CreateMatchCase6() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Estádio já tem partida no mesmo dia."))
                .when(validations).validateStadiumAvailable(stadium1, dto.matchDateTime());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Estádio já tem partida no mesmo dia.", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when stadium is not found")
    void CreateMatchCase7() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenThrow(new BadRequestException("Estádio não encontrado!"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Estádio não encontrado!", exception.getMessage());
    }

    @Test
    @DisplayName("should throw exception when club is not found")
    void CreateMatchCase8() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenThrow(new BadRequestException("Clube não encontrado!"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.createMatch(dto)
        );
        assertEquals("Clube não encontrado!", exception.getMessage());
    }

    @Test
    void updateMatch() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().plusDays(2), 3, 1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        match.setId(1L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);
        when(matchRepository.save(any(Match.class))).thenReturn(match);
        when(validations.formatResult(any())).thenReturn("3x1");
        when(validations.determineWinner(any())).thenReturn("Club 1");

        MatchesResponseDTO response = matchService.updateMatch(1L, dto);

        verify(validations).validateNotSameClubs(club1, club2);
        verify(validations).validateClubsActive(club1, club2);
        verify(validations).validateDateAfterFoundation(dto.matchDateTime(), club1, club2);
        verify(validations).validateNoNearMatches(club1, club2, dto.matchDateTime(), 1L);
        verify(validations).validateStadiumAvailable(stadium1, dto.matchDateTime(), 1L);

        assertNotNull(response);
        assertEquals("3x1", response.result());
        assertEquals("Club 1", response.winner());
    }

    @Test
    @DisplayName("should throw exception when match is not found")
    void updateMatchCase2() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().plusDays(2), 3, 1);

        when(matchRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                com.neocamp.api_futebol.exception.NotFoundException.class,
                () -> matchService.updateMatch(1L, dto)
        );

        assertEquals("Partida não encontrada!", ex.getMessage());
    }

   @Test
   @DisplayName("should throw exception when home and away clubs are the same")
    void updateMatchCase3() {
       MatchesRequestDTO dto = new MatchesRequestDTO(1L, 1L, 1L, LocalDateTime.now(), 2, 1);
       Match match = new Match(club1, club1, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
       match.setId(1L);

       when(matchRepository.findById(1L)).thenReturn(Optional.of(match));


       when(validations.findClubOrThrow(1L)).thenReturn(club1);
       when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

       doThrow(new BadRequestException("Clubes não podem ser iguais!"))
               .when(validations).validateNotSameClubs(club1, club1);

       BadRequestException exception = assertThrows(
               BadRequestException.class,
               () -> matchService.updateMatch(1L, dto)
       );
       assertEquals("Clubes não podem ser iguais!", exception.getMessage());
   }

   @Test
    @DisplayName("should throw exception when home club is inactive")
     void updateMatchCase4() {
       MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now(), 2, 1);
       Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
       match.setId(1L);

       when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

       when(validations.findClubOrThrow(1L)).thenReturn(club1);
       when(validations.findClubOrThrow(2L)).thenReturn(club2);
       when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

       club1.setActive(false);

       doThrow(new BadRequestException("Clube inativo!"))
               .when(validations).validateClubsActive(club1, club2);

       BadRequestException exception = assertThrows(
               BadRequestException.class,
               () -> matchService.updateMatch(1L, dto)
       );
       assertEquals("Clube inativo!", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when date is before home club foundation")
    void updateMatchCase5() {
         MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusYears(11), 2, 1);
         Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
         match.setId(1L);

         when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

         when(validations.findClubOrThrow(1L)).thenReturn(club1);
         when(validations.findClubOrThrow(2L)).thenReturn(club2);
         when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

         doThrow(new BadRequestException("Partida não pode ser criada antes da fundação de algum dos clubes!"))
                .when(validations).validateDateAfterFoundation(dto.matchDateTime(), club1, club2);

         BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.updateMatch(1L, dto)
         );
         assertEquals("Partida não pode ser criada antes da fundação de algum dos clubes!", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when match date is near another match date")
    void updateMatchCase6() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        match.setId(1L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Clubes possuem partidas próximas!"))
                .when(validations).validateNoNearMatches(club1, club2, dto.matchDateTime(), 1L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.updateMatch(1L, dto)
        );
        assertEquals("Clubes possuem partidas próximas!", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when stadium is not available")
    void updateMatchCase7() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        match.setId(1L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenReturn(stadium1);

        doThrow(new BadRequestException("Estádio já tem partida no mesmo dia."))
                .when(validations).validateStadiumAvailable(stadium1, dto.matchDateTime(), 1L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.updateMatch(1L, dto)
        );
        assertEquals("Estádio já tem partida no mesmo dia.", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when stadium is not found")
    void updateMatchCase8() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        match.setId(1L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenReturn(club2);
        when(validations.findStadiumOrThrow(1L)).thenThrow(new BadRequestException("Estádio não encontrado!"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.updateMatch(1L, dto)
        );
        assertEquals("Estádio não encontrado!", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when club is not found")
    void updateMatchCase9() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);
        Match match = new Match(club1, club2, stadium1, dto.matchDateTime(), dto.homeGoals(), dto.awayGoals());
        match.setId(1L);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        when(validations.findClubOrThrow(1L)).thenReturn(club1);
        when(validations.findClubOrThrow(2L)).thenThrow(new BadRequestException("Clube não encontrado!"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> matchService.updateMatch(1L, dto)
        );
        assertEquals("Clube não encontrado!", exception.getMessage());
   }

   @Test
   @DisplayName("should throw exception when match is not found")
    void updateMatchCase10() {
        MatchesRequestDTO dto = new MatchesRequestDTO(1L, 2L, 1L, LocalDateTime.now().minusDays(1), 2, 1);
        when(matchRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> matchService.updateMatch(1L, dto));
    }

    @Test
    @DisplayName("should return all matches when no filters applied")
    void searchMatches() {
        Match match = new Match(
                club1,
                club2,
                stadium1,
                LocalDateTime.now().minusDays(1),
                2,
                1
        );
        match.setId(1L);

        Page<Match> matchPage = new PageImpl<>(List.of(match)); // match é o mock do seu objeto Match
        when(matchRepository.findWithFilters(null, null, null, null, Pageable.unpaged()))
                .thenReturn(matchPage);

        when(validations.formatResult(any(Match.class))).thenReturn("2x1");
        when(validations.determineWinner(any(Match.class))).thenReturn("Club 1");


        Page<MatchesResponseDTO> result = matchService.searchMatches(null, null, null, null, Pageable.unpaged());


        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        MatchesResponseDTO dto = result.getContent().get(0);
        assertEquals("2x1", dto.result());
        assertEquals("Club 1", dto.winner());

        verify(matchRepository).findWithFilters(null, null, null, null, Pageable.unpaged());
        verify(validations).formatResult(match);
        verify(validations).determineWinner(match);
    }

    @Test
    @DisplayName("should throw NotFoundException when club does not exist")
    void searchMatchesCase2() {
        when(clubRepository.existsById(1L)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> matchService.searchMatches(1L, null, null, null, Pageable.unpaged())
        );
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    @DisplayName("should throw NotFoundException when stadium does not exist")
    void searchMatchesCase3() {
        when(stadiumRepository.existsById(1L)).thenReturn(false);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> matchService.searchMatches(null, 1L, null, null, Pageable.unpaged())
        );
        assertEquals("Estádio não encontrado!", ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException when side is invalid")
    void searchMatchesCase4() {
        when(clubRepository.existsById(anyLong())).thenReturn(true);
        when(stadiumRepository.existsById(anyLong())).thenReturn(true);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> matchService.searchMatches(1L, 1L, null, "invalid", Pageable.unpaged())
        );
        assertEquals("Lado inválido!", ex.getMessage());
    }



    @Test
    @DisplayName("should return matches filtered")
    void searchMatchesSuccess() {
        Match match = new Match(
                club1,
                club2,
                stadium1,
                LocalDateTime.now().minusDays(1),
                2,
                1
        );
        match.setId(1L);


        when(clubRepository.existsById(1L)).thenReturn(true);
        when(stadiumRepository.existsById(1L)).thenReturn(true);

        Page<Match> matchPage = new PageImpl<>(List.of(match));
        when(matchRepository.findWithFilters(1L, 1L, true, "casa", Pageable.unpaged())).thenReturn(matchPage);

        when(validations.formatResult(any(Match.class))).thenReturn("2x1");
        when(validations.determineWinner(any(Match.class))).thenReturn("Club 1");

        Page<MatchesResponseDTO> result = matchService.searchMatches(1L, 1L, true, "casa", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("2x1", result.getContent().get(0).result());
        assertEquals("Club 1", result.getContent().get(0).winner());
    }

    @Test
    @DisplayName("should return correct stats for home matches")
    void getClubRetrospective() {
        List<Match> matches = List.of(
                createMatch(3, 1, club1, club2),
                createMatch(2, 1, club1, club2),
                createMatch(0, 1, club1, club2)
        );
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(matchRepository.findAllHomeMatchesForClub(1L)).thenReturn(matches);

        MatchesRetrospectDTO dto = matchService.getClubRetrospective(1L, "casa");

        assertEquals("Club 1", dto.clubName());
        assertEquals(3, dto.matches());
        assertEquals(2, dto.victories());
        assertEquals(0, dto.draws());
        assertEquals(1, dto.defeats());
        assertEquals(5, dto.goalsFor());
        assertEquals(3, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should return correct stats for away matches")
    void getClubRetrospectiveCase2() {
        List<Match> matches = List.of(
                createMatch(1, 2, club2, club1),
                createMatch(1, 1, club2, club1),
                createMatch(2, 0, club2, club1)
        );
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(matchRepository.findAllAwayMatchesForClub(1L)).thenReturn(matches);
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1)); // para o segundo fetch

        MatchesRetrospectDTO dto = matchService.getClubRetrospective(1L, "fora");

        assertEquals("Club 1", dto.clubName());
        assertEquals(3, dto.matches());
        assertEquals(1, dto.victories());
        assertEquals(1, dto.draws());
        assertEquals(1, dto.defeats());
        assertEquals(3, dto.goalsFor());
        assertEquals(4, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should return correct stats for all matches (both sides)")
    void getClubRetrospectiveCase3() {

        List<Match> matches = List.of(
                createMatch(3, 1, club1, club2),
                createMatch(1, 1, club2, club1),
                createMatch(2, 0, club1, club2)
        );

        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(matchRepository.findAllMatchesForClub(1L)).thenReturn(matches);


        MatchesRetrospectDTO dto = matchService.getClubRetrospective(club1.getId(), null);

        assertEquals("Club 1", dto.clubName());
        assertEquals(3, dto.matches());
        assertEquals(2, dto.victories());
        assertEquals(1, dto.draws());
        assertEquals(0, dto.defeats());
        assertEquals(6, dto.goalsFor());
        assertEquals(2, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should throw NotFoundException when club does not exist (first check)")
    void getClubRetrospectiveCase4() {
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> matchService.getClubRetrospective(1L, null));
        assertEquals("Clube não encontrado!", ex.getMessage());
    }


    @Test
    @DisplayName("should throw BadRequestException for invalid side")
    void getClubRetrospectiveCase5() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> matchService.getClubRetrospective(1L, "invalid"));
        assertEquals("Lado inválido!", ex.getMessage());
    }


    @Test
    @DisplayName("should return correct stats for all matches between two clubs")
    void getOppRetrospect(){
        List<Match> matches = List.of(
                createMatch(3, 1, club1, club2),
                createMatch(2, 1, club1, club2),
                createMatch(0, 1, club1, club2)
        );
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club2));
        when(matchRepository.findAllMatchesBetweenClubs(1L, 2L, null)).thenReturn(matches);

        OppRetrospectDTO dto = matchService.getOneOppRestrospect(1L, 2L, null);

        assertEquals(2L, dto.opponentId());
        assertEquals("Club 2", dto.opponentName());
        assertEquals(3, dto.matches());
        assertEquals(2, dto.victories());
        assertEquals(0, dto.draws());
        assertEquals(1, dto.defeats());
        assertEquals(5, dto.goalsFor());
        assertEquals(3, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should return correct stats for all matches between two clubs when side is home")
    void getOppRetrospectCase2(){
        List<Match> matches = List.of(
                createMatch(3, 1, club1, club2),
                createMatch(2, 1, club1, club2),
                createMatch(0, 1, club1, club2)
        );
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club2));
        when(matchRepository.findAllMatchesBetweenClubs(1L, 2L, "casa")).thenReturn(matches);

        OppRetrospectDTO dto = matchService.getOneOppRestrospect(1L, 2L, "casa");

        assertEquals(2L, dto.opponentId());
        assertEquals("Club 2", dto.opponentName());
        assertEquals(3, dto.matches());
        assertEquals(2, dto.victories());
        assertEquals(0, dto.draws());
        assertEquals(1, dto.defeats());
        assertEquals(5, dto.goalsFor());
        assertEquals(3, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should return correct stats for all matches between two clubs when side is away")
    void getOppRetrospectCase3(){
        List<Match> matches = List.of(
                createMatch(3, 1, club1, club2),
                createMatch(2, 1, club1, club2),
                createMatch(0, 1, club1, club2)
        );
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club2));
        when(matchRepository.findAllMatchesBetweenClubs(1L, 2L, "fora")).thenReturn(matches);

        OppRetrospectDTO dto = matchService.getOneOppRestrospect(1L, 2L, "fora");

        assertEquals(2L, dto.opponentId());
        assertEquals("Club 2", dto.opponentName());
        assertEquals(3, dto.matches());
        assertEquals(2, dto.victories());
        assertEquals(0, dto.draws());
        assertEquals(1, dto.defeats());
        assertEquals(5, dto.goalsFor());
        assertEquals(3, dto.goalsAgainst());
    }

    @Test
    @DisplayName("should throw NotFoundException when opponent does not exist")
    void getOppRetrospectCase4(){
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(clubRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> matchService.getOneOppRestrospect(1L, 2L, null));
        assertEquals("Adversário não encontrado!", ex.getMessage());
    }

    @Test
    @DisplayName("should throw NotFoundException when home club does not exist")
    void getOppRetrospectCase5(){
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> matchService.getOneOppRestrospect(1L, 2L, null));
        assertEquals("Clube não encontrado!", ex.getMessage());
    }

    @Test
    @DisplayName("should throw BadRequestException when side is invalid")
    void getOppRetrospectCase6(){
        when(clubRepository.findById(1L)).thenReturn(Optional.of(club1));
        when(clubRepository.findById(2L)).thenReturn(Optional.of(club2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> matchService.getOneOppRestrospect(1L, 2L, "invalid"));
        assertEquals("Lado inválido!", ex.getMessage());
    }




    private Match createMatch(int homeGoals, int awayGoals, Club clubMatch1, Club clubMatch2) {
        return new Match(clubMatch1, clubMatch2, stadium1, LocalDateTime.now().minusDays(10), homeGoals, awayGoals);
    }



}
