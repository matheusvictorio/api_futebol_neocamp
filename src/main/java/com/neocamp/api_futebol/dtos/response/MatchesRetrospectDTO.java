package com.neocamp.api_futebol.dtos.response;

public record MatchesRetrospectDTO(
        String clubName,
        int matches,
        int victories,
        int draws,
        int defeats,
        int goalsFor,
        int goalsAgainst
) {}
