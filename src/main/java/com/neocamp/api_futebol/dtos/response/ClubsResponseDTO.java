package com.neocamp.api_futebol.dtos.response;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.State;

import java.time.LocalDate;

public record ClubsResponseDTO(
        Long id,
        String name,
        State state,
        Boolean active,
        LocalDate createdAt
) {
    public ClubsResponseDTO(Club club){
        this(club.getId(), club.getName(), club.getState(), club.getActive(), club.getCreatedAt());
    }
}
