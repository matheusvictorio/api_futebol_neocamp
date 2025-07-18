package com.neocamp.api_futebol.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.MatchesRetrospectDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.Match;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.entities.State;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import com.neocamp.api_futebol.services.MatchService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ClubControllerTest {
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

    private Club club;
    @Autowired
    private MatchService matchService;

    @BeforeEach
    void setUp() {
        clubRepository.deleteAll();
        club = new Club();
        club.setName("Test Club");
        club.setState(State.SP);
        club.setActive(true);
        club = clubRepository.save(club);
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



    private Stadium createStadium(String name) {
        Stadium stadium = new Stadium();
        stadium.setName(name);
        stadium.setActive(true);
        return stadiumRepository.save(stadium);
    }

    @Test
    @DisplayName("should create a club")
    void createClub() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO("Novo Clube", State.RJ, LocalDate.now().minusDays(1));
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Clube"));
    }

    @Test
    @DisplayName("should return 409 when try to create a club with same name and state")
    void createClubCase2() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO("Test Club", State.SP, LocalDate.now().minusDays(1));
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 400 when try to create a club with invalid date")
    void createClubWithCase3() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO(null, null, LocalDate.now().plusDays(2));
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when try to create a club with invalid name")
    void createClubWithCase4() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO(null, State.SP, LocalDate.now().minusDays(1));
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when try to create a club with invalid state")
    void createClubWithCase5() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO("Test Club", null, LocalDate.now().minusDays(1));
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when try to create a club with invalid state case 2")
    void createClubWithCase6() throws Exception {
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Club\", \"state\":\"INVALID_STATE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Estádo inválido")));
    }


    @Test
    @DisplayName("should uptade a club")
    void updateClub() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO("Clube Atualizado", State.MG, LocalDate.now().minusDays(1));
        mockMvc.perform(put("/clubs/" + club.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clube Atualizado"));
    }

    @Test
    @DisplayName("should return 404 when try to update a club that not exists")
    void updateClubCase2() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO("Test Club", State.SP, LocalDate.now().minusDays(1));
        mockMvc.perform(put("/clubs/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 409 when try to update a club with same name and state")
    void updateClubCase3() throws Exception {
        ClubsRequestDTO club2DTO = new ClubsRequestDTO("Another Club", State.SP, LocalDate.now().minusDays(4));
        String club2Response = mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(club2DTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long club2Id = objectMapper.readTree(club2Response).get("id").asLong();

        // Tenta atualizar o segundo clube para o mesmo nome/estado do primeiro
        ClubsRequestDTO dtoComConflito = new ClubsRequestDTO("Test Club", State.SP, LocalDate.now().minusDays(4));
        mockMvc.perform(put("/clubs/" + club2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoComConflito)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 409 when updating createdAt to after any existing match")
    void updateClubCase4() throws Exception {
        // 1. Clube criado em 2024-06-01
        LocalDate originalCreatedAt = LocalDate.of(2024, 6, 1);
        Club club = new Club("Clube Teste Conflito", State.SP, originalCreatedAt);
        club.setActive(true);
        club = clubRepository.save(club);

        // 2. Partida em 2024-06-05
        Club assistantClub = new Club("Outro Clube", State.RJ, originalCreatedAt);
        assistantClub.setActive(true);
        assistantClub = clubRepository.save(assistantClub);

        Stadium stadium = createStadium("Estádio");
        Match match = createMatch(club, assistantClub, stadium,
                LocalDateTime.of(2024, 6, 5, 15, 0), 1, 0);

        // 3. Tenta atualizar createdAt para depois da partida (2024-06-10)
        ClubsRequestDTO request = new ClubsRequestDTO(
                club.getName(),
                club.getState(),
                LocalDate.of(2024, 6, 10)  // NOVA DATA DE CRIAÇÃO
        );

        mockMvc.perform(put("/clubs/" + club.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return 400 when try to update a club with invalid data")
    void updateClubCase5() throws Exception {
        ClubsRequestDTO dto = new ClubsRequestDTO(null, null, LocalDate.now().plusDays(2));
        mockMvc.perform(put("/clubs/" + club.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should delete a club")
    void deleteClub() throws Exception {
        mockMvc.perform(delete("/clubs/" + club.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return 404 when try to delete a club that not exists")
    void deleteCase2() throws Exception {
        mockMvc.perform(delete("/clubs/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 400 when try to delete a club that is already inactive")
    void deleteCase3() throws Exception {
        club.setActive(false);
        clubRepository.save(club);
        mockMvc.perform(delete("/clubs/" + club.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 200 when try to get a club")
    void getClub() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Club"));
    }
    @Test
    @DisplayName("should return 404 when try to get a club that not exists")
    void getClubCase2() throws Exception {
        mockMvc.perform(get("/clubs/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 200 when try to get all clubs")
    void getAll() throws Exception {
        mockMvc.perform(get("/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("should return 200 when try to get all club retrospective")
    void getClubRetrospective() throws Exception {
        Club club2 = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(club, club2, createStadium("Estádio"), LocalDateTime.now(), 1, 0);
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(1))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(1))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get all club retrospective when aways goals > home goals")
    void getClubRetrospectiveCase2() throws Exception {
        Club club2 = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(club2, club, createStadium("Estádio"), LocalDateTime.now(), 0, 1);
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(1))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(1))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get all club retrospective with a draw")
    void getClubRetrospectiveCase3() throws Exception {
        Club club2 = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(club2, club, createStadium("Estádio"), LocalDateTime.now(), 0, 0);
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(0))
                .andExpect(jsonPath("$.draws").value(1))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(0))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get all club retrospective with a defeat")
    void getClubRetrospectiveCase4() throws Exception {
        Club club2 = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(club, club2, createStadium("Estádio"), LocalDateTime.now(), 0, 1);
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(0))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(1))
                .andExpect(jsonPath("$.goalsFor").value(0))
                .andExpect(jsonPath("$.goalsAgainst").value(1));
    }

    @Test
    @DisplayName("should return 404 when try to get club retrospective that not exists")
    void getClubRetrospectiveCase5() throws Exception {
        mockMvc.perform(get("/clubs/99999/retrospect"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 200 when try to get club retrospective with side=fora")
    void getClubRetrospectiveCase6() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect?side=fora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()));
    }
    @Test
    @DisplayName("should return 200 when try to get club retrospective with side=casa")
    void getClubRetrospectiveCase7() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect?side=casa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubName").value(club.getName()));
    }
    @Test
    @DisplayName("should return 400 when try to get club retrospective with invalid side")
    void getClubRetrospectiveCase8() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/retrospect?side=invalido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 200 when try to get club opp retrospective")
    void getClubOppRetrospective() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("should return 200 when try to get club opp retrospective with side=casa")
    void getClubOppRetrospectiveCase2() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/retrospect?side=casa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("should return 200 when try to get club opp retrospective with side=fora")
    void getClubOppRetrospectiveCase3() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/retrospect?side=fora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("should return 400 when try to get club opp retrospective with invalid side")
    void getClubOppRetrospectiveCase4() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/retrospect?side=invalido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective")
    void getClubOneOppRetrospective() throws Exception {
        Club opp = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));

        Match match = createMatch(club, opp, createStadium("Estádio"), LocalDateTime.now(), 1, 0);

        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()))
                .andExpect(jsonPath("$.opponentName").value(opp.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(1))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(1))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective when away goals > home goals")
    void getClubOneOppRetrospectiveCase2() throws Exception {
        Club opp = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(opp, club, createStadium("Estádio"), LocalDateTime.now(), 0, 1);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()))
                .andExpect(jsonPath("$.opponentName").value(opp.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(1))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(1))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective with a draw")
    void getClubOneOppRetrospectiveCase3() throws Exception {
        Club opp = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(opp, club, createStadium("Estádio"), LocalDateTime.now(), 0, 0);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()))
                .andExpect(jsonPath("$.opponentName").value(opp.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(0))
                .andExpect(jsonPath("$.draws").value(1))
                .andExpect(jsonPath("$.defeats").value(0))
                .andExpect(jsonPath("$.goalsFor").value(0))
                .andExpect(jsonPath("$.goalsAgainst").value(0));
    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective with a defeat")
    void getClubOneOppRetrospectiveCase4() throws Exception {
        Club opp = clubRepository.save(new Club("Clube Adversario", State.RJ, LocalDate.now().minusDays(1)));
        Match match = createMatch(club, opp, createStadium("Estádio"), LocalDateTime.now(), 0, 1);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()))
                .andExpect(jsonPath("$.opponentName").value(opp.getName()))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.victories").value(0))
                .andExpect(jsonPath("$.draws").value(0))
                .andExpect(jsonPath("$.defeats").value(1))
                .andExpect(jsonPath("$.goalsFor").value(0))
                .andExpect(jsonPath("$.goalsAgainst").value(1));
    }


    @Test
    @DisplayName("should return 404 when try to get club one opp retrospective with invalid opp id")
    void getClubOneOppRetrospectiveCase5() throws Exception {
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/9999/retrospect"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 404 when try to get club one opp retrospective with invalid club id")
    void getClubOneOppRetrospectiveCase6() throws Exception {
        mockMvc.perform(get("/clubs/9999/opp/" + club.getId() + "/retrospect"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective with side=casa")
    void getClubOneOppRetrospectiveCase7() throws Exception {
        Club opp = new Club();
        opp.setName("Clube Adversario");
        opp.setState(State.RJ);
        opp.setActive(true);
        opp = clubRepository.save(opp);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect?side=casa") )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()));

    }

    @Test
    @DisplayName("should return 200 when try to get club one opp retrospective with side=fora")
    void getClubOneOppRetrospectiveCase8() throws Exception {
        Club opp = new Club();
        opp.setName("Clube Adversario");
        opp.setState(State.RJ);
        opp.setActive(true);
        opp = clubRepository.save(opp);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect?side=fora") )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.opponentId").value(opp.getId()));

    }

    @Test
    @DisplayName("should return 400 when try to get club one opp retrospective with invalid side")
    void getClubOneOppRetrospectiveCase9() throws Exception {
        Club opp = new Club();
        opp.setName("Clube Adversario");
        opp.setState(State.RJ);
        opp.setActive(true);
        opp = clubRepository.save(opp);
        mockMvc.perform(get("/clubs/" + club.getId() + "/opp/" + opp.getId() + "/retrospect?side=invalido") )
                .andExpect(status().isBadRequest());
    }
}