package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.dtos.response.StadiumResponseDTO;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.BadRequestException;
import com.neocamp.api_futebol.exception.ConflictException;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StadiumService {
    @Autowired
    private StadiumRepository stadiumRepository;

    public StadiumResponseDTO createStadium(StadiumRequestDTO stadiumRequestDTO) {
        if (stadiumRepository.existsByNameIgnoreCase(stadiumRequestDTO.name())){
            throw new ConflictException("Estádio ja existe!");
        }
        Stadium stadium = new Stadium();
        stadium.setName(stadiumRequestDTO.name());
        stadiumRepository.save(stadium);
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getActive());
    }

    public StadiumResponseDTO updateStadium(Long id, @Valid StadiumRequestDTO stadiumRequestDTO) {
        Stadium stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado!"));

        if (stadiumRepository.existsByNameIgnoreCase(stadiumRequestDTO.name())) {
            throw new ConflictException("Estádio ja existe!");
        }
        stadium.setName(stadiumRequestDTO.name());
        stadiumRepository.save(stadium);
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getActive());
    }

    public void deleteStadium(Long id) {
        var stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado"));
        if (!stadium.getActive()) {
            throw new ConflictException("Clube já está inativo!");
        }
        stadium.delete();
        stadiumRepository.save(stadium);
    }

    public StadiumResponseDTO findById(Long id) {
        var stadium =  stadiumRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado ou inativo"));;
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getActive());
    }

    public Page<StadiumResponseDTO> searchStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable)
                .map(StadiumResponseDTO::new);
    }
}
