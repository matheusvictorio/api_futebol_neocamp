package com.neocamp.api_futebol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.entities.State;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private StadiumRepository stadiumRepository;

    private Club clubA, clubB;

    private Match match1, match2, match3;

    private Stadium stadium;

    @BeforeEach
    void setUp() {
        clubA = new Club("Clube A", State.SP, java.time.LocalDate.now().minusYears(10));
        clubB = new Club("Clube B", State.RJ, java.time.LocalDate.now().minusYears(8));
        clubA = clubRepository.save(clubA);
        clubB = clubRepository.save(clubB);

        // Create stadium
        stadium = new Stadium("Estádio Teste");
        stadium = stadiumRepository.save(stadium);

        // Create matches
        match1 = new Match(clubA, clubB, stadium, java.time.LocalDateTime.now().minusDays(3), 2, 1);
        match2 = new Match(clubB, clubA, stadium, java.time.LocalDateTime.now(), 0, 3);
        match3 = new Match(clubA, clubB, stadium, java.time.LocalDateTime.now().minusDays(5), 0, 1);
        matchRepository.save(match1);
        matchRepository.save(match2);
        matchRepository.save(match3);
    }


    @Test
    @DisplayName("Should return ranking ordered by points")
    void getRanking() throws Exception {

        mockMvc.perform(get("/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clubId").value(clubA.getId()))
                .andExpect(jsonPath("$[0].clubName").value(clubA.getName()))
                .andExpect(jsonPath("$.[0].points").value(6))
                .andExpect(jsonPath("$.[0].goals").value(5))
                .andExpect(jsonPath("$.[0].victories").value(2))
                .andExpect(jsonPath("$.[0].matches").value(3))

                .andExpect(jsonPath("$.[1].clubId").value(clubB.getId()))
                .andExpect(jsonPath("$[1].clubName").value(clubB.getName()))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].goals").value(2))
                .andExpect(jsonPath("$.[1].victories").value(1))
                .andExpect(jsonPath("$.[1].matches").value(3));

    }

    @Test
    @DisplayName("Should return ranking ordered by goals")
    void getRankingCase2() throws Exception {

        mockMvc.perform(get("/ranking?filter=gols"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clubId").value(clubA.getId()))
                .andExpect(jsonPath("$[0].clubName").value(clubA.getName()))
                .andExpect(jsonPath("$.[0].points").value(6))
                .andExpect(jsonPath("$.[0].goals").value(5))
                .andExpect(jsonPath("$.[0].victories").value(2))
                .andExpect(jsonPath("$.[0].matches").value(3))

                .andExpect(jsonPath("$.[1].clubId").value(clubB.getId()))
                .andExpect(jsonPath("$[1].clubName").value(clubB.getName()))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].goals").value(2))
                .andExpect(jsonPath("$.[1].victories").value(1))
                .andExpect(jsonPath("$.[1].matches").value(3));

    }

    @Test
    @DisplayName("Should return ranking ordered by victories")
    void getRankingCase3() throws Exception {

        mockMvc.perform(get("/ranking?filter=vitorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clubId").value(clubA.getId()))
                .andExpect(jsonPath("$[0].clubName").value(clubA.getName()))
                .andExpect(jsonPath("$.[0].points").value(6))
                .andExpect(jsonPath("$.[0].goals").value(5))
                .andExpect(jsonPath("$.[0].victories").value(2))
                .andExpect(jsonPath("$.[0].matches").value(3))

                .andExpect(jsonPath("$.[1].clubId").value(clubB.getId()))
                .andExpect(jsonPath("$[1].clubName").value(clubB.getName()))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].goals").value(2))
                .andExpect(jsonPath("$.[1].victories").value(1))
                .andExpect(jsonPath("$.[1].matches").value(3));

    }

    @Test
    @DisplayName("Should return ranking ordered by matches")
    void getRankingCase4() throws Exception {
        mockMvc.perform(get("/ranking?filter=partidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].clubId").value(clubA.getId()))
                .andExpect(jsonPath("$[0].clubName").value(clubA.getName()))
                .andExpect(jsonPath("$.[0].points").value(6))
                .andExpect(jsonPath("$.[0].goals").value(5))
                .andExpect(jsonPath("$.[0].victories").value(2))
                .andExpect(jsonPath("$.[0].matches").value(3))

                .andExpect(jsonPath("$.[1].clubId").value(clubB.getId()))
                .andExpect(jsonPath("$[1].clubName").value(clubB.getName()))
                .andExpect(jsonPath("$.[1].points").value(3))
                .andExpect(jsonPath("$.[1].goals").value(2))
                .andExpect(jsonPath("$.[1].victories").value(1))
                .andExpect(jsonPath("$.[1].matches").value(3));
    }

    @Test
    @DisplayName("Should return bad request when filter is invalid")
    void getRankingCase5() throws Exception {
        mockMvc.perform(get("/ranking?filter=invalido"))
                .andExpect(status().isBadRequest());
    }
}