package com.neocamp.api_futebol.dtos.response;

import com.neocamp.api_futebol.entities.Stadium;

public record StadiumResponseDTO(
        Long id,
        String name,
        Boolean active
) {
    public StadiumResponseDTO(Stadium stadium){
       this(stadium.getId(), stadium.getName(), stadium.getActive());
    }
}
