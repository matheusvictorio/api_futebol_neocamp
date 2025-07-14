package com.neocamp.api_futebol.dtos.response;

public record OppRetrospectDTO(
        Long opponentId,
        String opponentName,
        Long matches,
        Long victories,
        Long draws,
        Long defeats,
        Long goalsFor,
        Long goalsAgainst
) {}
