package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.dtos.response.StadiumResponseDTO;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StadiumService {
    @Autowired
    private StadiumRepository stadiumRepository;

    public StadiumResponseDTO createStadium(StadiumRequestDTO stadiumRequestDTO) {
        if (stadiumRepository.existsByNameIgnoreCase(stadiumRequestDTO.name())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estádio ja existe!");
        }
        Stadium stadium = new Stadium();
        stadium.setName(stadiumRequestDTO.name());
        stadiumRepository.save(stadium);
        return new StadiumResponseDTO(stadium.getId(), stadium.getName());
    }
}
