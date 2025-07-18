package com.neocamp.api_futebol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.entities.State;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class MatchControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private StadiumRepository stadiumRepository;
    @Autowired
    private MatchRepository matchRepository;

    private Club club1, club2;

    private Stadium stadium;

    private Match match;

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

    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        stadiumRepository.deleteAll();
        club1 = clubRepository.save(new Club("Flamengo", State.RJ, java.time.LocalDate.now().minusDays(4)));
        club2 = clubRepository.save(new Club("Vasco", State.RJ, java.time.LocalDate.now().minusDays(4)));
        stadium = stadiumRepository.save(new Stadium("Maracanã"));
    }


    @Test
    @DisplayName("should return 201 create a match")
    void createMatch() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId(), java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should return 400 when club is not found")
    void createMatchCase2() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(9999L, club2.getId(), stadium.getId(), java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when stadium is not found")
    void createMatchCase3() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), 9999L, java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when home club is the same as away club")
    void createMatchCase4() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club1.getId(), stadium.getId(), java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 409 when home club is inactive")
    void createMatchCase5() throws Exception {
        club1.setActive(false);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId(), java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("should return 409 when match date is before club foundation")
    void createMatchCase6() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId(), java.time.LocalDateTime.now().minusDays(5), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("should return 409 because clubs have near matches")
    void createMatchCase7() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId(), java.time.LocalDateTime.now().minusDays(1), 3, 2);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 409 because stadium have near matches")
    void createMatchCase8() throws Exception {
        Club club3 = clubRepository.save(new Club("Fluminense", State.RJ, java.time.LocalDate.now().minusDays(4)));
        Club club4 = clubRepository.save(new Club("Botafogo", State.RJ, java.time.LocalDate.now().minusDays(4)));
        Match match = createMatch(club3, club4, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId(), java.time.LocalDateTime.now(), 2, 1);
        mockMvc.perform(post("/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }




    @Test
    @DisplayName("should return 200 when update a match")
    void updateMatch() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return 404 when update a match that not exists")
    void updateMatchCase2() throws Exception {
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 400 when club is not found")
    void updateMatchCase3() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(9999L, club2.getId(), stadium.getId() , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when stadium is not found")
    void updateMatchCase4() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), 9999L , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when home club is the same as away club")
    void updateMatchCase5() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club1.getId(), stadium.getId() , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 409 when home club is inactive")
    void updateMatchCase6() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        club1.setActive(false);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now(), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 409 when match date is before club foundation")
    void updateMatchCase7() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now().minusDays(5), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 409 because clubs have near matches")
    void updateMatchCase8() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        Match match2 = createMatch(club1, club2, stadium, LocalDateTime.now().minusDays(3), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now().minusDays(2), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 409 because stadium have near matches")
    void updateMatchCase9() throws Exception {
        Club club3 = clubRepository.save(new Club("Fluminense", State.RJ, java.time.LocalDate.now().minusDays(4)));
        Club club4 = clubRepository.save(new Club("Botafogo", State.RJ, java.time.LocalDate.now().minusDays(4)));
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        Match match2 = createMatch(club3, club4, stadium, LocalDateTime.now().minusDays(3), 2, 1);
        MatchesRequestDTO dto = new MatchesRequestDTO(club1.getId(), club2.getId(), stadium.getId() , java.time.LocalDateTime.now().minusDays(3), 3, 2);
        mockMvc.perform(put("/matches/" + match.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 204 when delete a match")
    void deleteMatch() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        mockMvc.perform(delete("/matches/" + match.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return 404 when delete a match that not exists")
    void deleteMatchCase2() throws Exception {
        mockMvc.perform(delete("/matches/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 200 when get a match")
    void getMatch() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        mockMvc.perform(get("/matches/" + match.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(match.getId()))
                .andExpect(jsonPath("$.homeClub").value(club1.getName()))
                .andExpect(jsonPath("$.awayClub").value(club2.getName()))
                .andExpect(jsonPath("$.stadium").value(stadium.getName()))
                .andExpect(jsonPath("$.matchDateTime").value(match.getMatchDateTime().toString()))
                .andExpect(jsonPath("$.result").value("2 x 1"))
                .andExpect(jsonPath("$.winner").value(club1.getName()));
    }

    @Test
    @DisplayName("should return 200 when get a match with a away victory")
    void getMatchCase2() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 1, 2);
        mockMvc.perform(get("/matches/" + match.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(match.getId()))
                .andExpect(jsonPath("$.homeClub").value(club1.getName()))
                .andExpect(jsonPath("$.awayClub").value(club2.getName()))
                .andExpect(jsonPath("$.stadium").value(stadium.getName()))
                .andExpect(jsonPath("$.matchDateTime").value(match.getMatchDateTime().toString()))
                .andExpect(jsonPath("$.result").value("1 x 2"))
                .andExpect(jsonPath("$.winner").value(club2.getName()));
    }

    @Test
    @DisplayName("should return 200 when get a match with no goals")
    void getMatchCase3() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 0, 0);
        mockMvc.perform(get("/matches/" + match.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(match.getId()))
                .andExpect(jsonPath("$.homeClub").value(club1.getName()))
                .andExpect(jsonPath("$.awayClub").value(club2.getName()))
                .andExpect(jsonPath("$.stadium").value(stadium.getName()))
                .andExpect(jsonPath("$.matchDateTime").value(match.getMatchDateTime().toString()))
                .andExpect(jsonPath("$.result").value("0 x 0"))
                .andExpect(jsonPath("$.winner").value("Empate"));
    }

    @Test
    @DisplayName("should return 404 when get a match that not exists")
    void getMatchCase4() throws Exception {
        mockMvc.perform(get("/matches/9999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("should return 200 when get all matches")
    void getAll() throws Exception {
        Match match = createMatch(club1, club2, stadium, LocalDateTime.now(), 2, 1);
        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(match.getId()))
                .andExpect(jsonPath("$.content[0].homeClub").value(club1.getName()))
                .andExpect(jsonPath("$.content[0].awayClub").value(club2.getName()))
                .andExpect(jsonPath("$.content[0].stadium").value(stadium.getName()))
                .andExpect(jsonPath("$.content[0].matchDateTime").value(match.getMatchDateTime().toString()))
                .andExpect(jsonPath("$.content[0].result").value("2 x 1"))
                .andExpect(jsonPath("$.content[0].winner").value(club1.getName()));
    }


    @Test
    @DisplayName("should return 200 when get all matches with all filters")
    void getAllCase2() throws Exception {
        mockMvc.perform(get("/matches?" + club1.getId() + "&" + stadium.getId()+ "&routs=true&side=casa"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return 400 for invalid side value")
    void getAllWithInvalidSide() throws Exception {
        mockMvc.perform(get("/matches?side=invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 404 for a club id that not exists")
    void getAllWithInvalidClubId() throws Exception {
        mockMvc.perform(get("/matches?clubId=9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 404 for a stadium id that not exists")
    void getAllWithInvalidStadiumId() throws Exception {
        mockMvc.perform(get("/matches?stadiumId=9999"))
                .andExpect(status().isNotFound());
    }
}