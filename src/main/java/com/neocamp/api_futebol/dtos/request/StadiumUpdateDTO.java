package com.neocamp.api_futebol.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StadiumUpdateDTO(
        @Size(min = 3, message = "Nome deve ter pelo menos 2 letras!")
        String name,
        @Pattern(regexp = "^[0-9]{5}-?[0-9]{3}$",
                message = "CEP deve estar no formato 99999-999 ou 99999999")
        String cep
) {
}
