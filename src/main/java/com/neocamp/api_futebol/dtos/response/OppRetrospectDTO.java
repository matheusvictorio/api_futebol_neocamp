package com.neocamp.api_futebol.dtos.response;

public record OppRetrospectDTO(
        Long opponentId,
        String opponentName,
        int victories,
        int draws,
        int defeats,
        int goalsFor,
        int goalsAgainst
) {
}
