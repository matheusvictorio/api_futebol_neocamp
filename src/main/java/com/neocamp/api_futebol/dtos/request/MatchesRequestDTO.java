package com.neocamp.api_futebol.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neocamp.api_futebol.entities.Club;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDateTime;

public record MatchesRequestDTO(
        @NotNull(message = "Id do clube da casa é obrigatório!")
        Long homeClubId,
        @NotNull(message = "Id do clube de fora é obrigatório!")
        Long awayClubId,
        @NotNull(message = "Id do estádio é obrigatório!")
        Long stadiumId,
        @NotNull(message = "Data da partida é obrigatória!")
        @Past(message = "Data da partida não pode ser no futuro!")
        @JsonFormat(pattern = "dd-MM-yyyy'T'HH:mm:ss")
        LocalDateTime matchDateTime,
        @NotNull @Min(value = 0, message = "O saldo de gols não pode ser negativo!")
        Integer homeGoals,
        @NotNull @Min(value = 0, message = "O saldo de gols não pode ser negativo!")
        Integer awayGoals
) {}