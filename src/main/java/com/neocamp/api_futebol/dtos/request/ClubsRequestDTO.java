package com.neocamp.api_futebol.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neocamp.api_futebol.entities.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClubsRequestDTO(
        @NotBlank(message = "Nome do clube é obrigatório!")
        @Size(min = 2, message = "Nome deve ter pelo menos 2 letras")
        String name,
        @NotNull(message = "Estado é obrigatório!")
        State state,
        @NotNull(message = "data de criação é obrigatória!")
        @Past(message = "Data de criação não pode ser no futuro")
        @JsonFormat(pattern = "dd-MM-yyyy" )
        LocalDate createdAt
) {
    public ClubsRequestDTO(String name, State state, LocalDate createdAt) {
        this.name = name;
        this.state = state;
        this.createdAt = createdAt;
    }
}
