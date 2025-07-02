package com.neocamp.api_futebol.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StadiumRequestDTO(
        @NotBlank(message = "Nome do estádio é obrigatório!")
        @Size(min = 3, message = "Nome deve ter pelo menos 2 letras!")
        String name
) {
}
