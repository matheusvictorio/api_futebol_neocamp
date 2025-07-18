package com.neocamp.api_futebol.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class StadiumControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StadiumRepository stadiumRepository;


    @Test
    @DisplayName("should create a stadium")
    void createStadium() throws Exception {
        StadiumRequestDTO dto = new StadiumRequestDTO("Estádio");
        mockMvc.perform(post("/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Estádio"))
                .andExpect(jsonPath("$.active").value(true));
    }
    @Test
    @DisplayName("should return 400 when try to create a stadium with empty name")
    void createStadiumCase2() throws Exception {
        StadiumRequestDTO dto = new StadiumRequestDTO("");
        mockMvc.perform(post("/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("should return 400 when try to create a stadium with one character name")
    void createStadiumCase3() throws Exception {
        StadiumRequestDTO dto = new StadiumRequestDTO("a");
        mockMvc.perform(post("/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 409 when try to create a stadium with existing name")
    void createStadiumCase4() throws Exception {

        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        StadiumRequestDTO dto = new StadiumRequestDTO("Estádio");

        mockMvc.perform(post("/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should update a stadium")
    void updateStadium() throws Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        StadiumRequestDTO dto = new StadiumRequestDTO("Estádio Atualizado");
        mockMvc.perform(put("/stadiums/{id}", stadium.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stadium.getId()))
                .andExpect(jsonPath("$.name").value("Estádio Atualizado"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("should return 404 when try to update a stadium that does not exist")
    void updateStadiumCase2() throws Exception {
        StadiumRequestDTO dto = new StadiumRequestDTO("Estádio Atualizado");
        mockMvc.perform(put("/stadiums/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 409 when try to update a stadium with existing name")
    void updateStadiumCase3() throws Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio Atualizado"));
        Stadium stadium2 = stadiumRepository.save(new Stadium("Estádio"));
        StadiumRequestDTO dto = new StadiumRequestDTO("Estádio Atualizado");
        mockMvc.perform(put("/stadiums/{id}", stadium2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should delete a stadium")
    void deleteStadium() throws Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        mockMvc.perform(delete("/stadiums/{id}", stadium.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return 404 when try to delete a stadium that does not exist")
    void deleteStadiumCase2() throws Exception {
        mockMvc.perform(delete("/stadiums/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 409 when try to delete a stadium that is already inactive")
    void deleteStadiumCase3() throws Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        stadium.delete();
        mockMvc.perform(delete("/stadiums/{id}", stadium.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return a stadium by id")
    void getStadium() throws  Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        mockMvc.perform(get("/stadiums/{id}", stadium.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stadium.getId()))
                .andExpect(jsonPath("$.name").value(stadium.getName()))
                .andExpect(jsonPath("$.active").value(stadium.getActive()));
    }

    @Test
    @DisplayName("should return 404 when try to get a stadium that does not exist")
    void getStadiumCase2() throws  Exception {
        mockMvc.perform(get("/stadiums/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return 404 when try to get a stadium that is already inactive")
    void getStadiumCase3() throws  Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        stadium.delete();
        mockMvc.perform(get("/stadiums/{id}", stadium.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should return a list of stadiums")
    void getStadiums() throws Exception {
        Stadium stadium = stadiumRepository.save(new Stadium("Estádio"));
        Stadium stadium2 = stadiumRepository.save(new Stadium("Estádio 2"));

        mockMvc.perform(get("/stadiums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(stadium.getId()))
                .andExpect(jsonPath("$.content[0].name").value(stadium.getName()))
                .andExpect(jsonPath("$.content[0].active").value(stadium.getActive()))
                .andExpect(jsonPath("$.content[1].id").value(stadium2.getId()))
                .andExpect(jsonPath("$.content[1].name").value(stadium2.getName()))
                .andExpect(jsonPath("$.content[1].active").value(stadium2.getActive()));

    }
}