package com.neocamp.api_futebol.dtos.response;

public record StadiumResponseDTO(
        Long id,
        String name,
        Boolean active
) {
}
