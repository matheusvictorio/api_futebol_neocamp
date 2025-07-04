package com.neocamp.api_futebol.dtos.response;

public record ClubRankingDTO(Long clubId,
                             String clubName,
                             Long points,
                             Long goals,
                             Long victories,
                             Long matches) {
}
