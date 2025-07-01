package com.neocamp.api_futebol.dtos.response;

import java.time.LocalDateTime;

public record MatchesResponseDTO(
        Long id,
        String homeClub,
        String awayClub,
        String stadium,
        LocalDateTime matchDateTime,
        String result,
        String winner
) {
}
