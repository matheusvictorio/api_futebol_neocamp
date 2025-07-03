package com.neocamp.api_futebol.dtos.response;

public record MatchesRetrospectDTO(
        int victories,
        int draws,
        int defeats,
        int goalsFor,
        int goalsAgainst
) {}
