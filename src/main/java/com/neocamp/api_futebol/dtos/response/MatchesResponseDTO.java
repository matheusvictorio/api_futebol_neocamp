package com.neocamp.api_futebol.dtos.response;

import com.neocamp.api_futebol.entities.Match;

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
    public MatchesResponseDTO(Match match, String result, String winner) {
        this(match.getId(), match.getHomeClub().getName(), match.getAwayClub().getName(), match.getStadium().getName(), match.getMatchDateTime(), result, winner);
    }
}
