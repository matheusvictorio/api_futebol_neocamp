package com.neocamp.api_futebol.services;

import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.dtos.request.StadiumUpdateDTO;
import com.neocamp.api_futebol.dtos.response.StadiumResponseDTO;
import com.neocamp.api_futebol.entities.Address;
import com.neocamp.api_futebol.entities.Stadium;
import com.neocamp.api_futebol.exception.ConflictException;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.repositories.StadiumRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StadiumService {
    @Autowired
    private StadiumRepository stadiumRepository;
    @Autowired
    private ViaCepService viaCepService;


    public StadiumResponseDTO createStadium(StadiumRequestDTO stadiumRequestDTO) {
        if (stadiumRepository.existsByNameIgnoreCase(stadiumRequestDTO.name())){
            throw new ConflictException("Estádio ja existe!");
        }

        Address address = viaCepService.findByCep(stadiumRequestDTO.cep().replace("-", ""));

        Stadium stadium = new Stadium();
        stadium.setName(stadiumRequestDTO.name());
        stadium.setAddress(address);
        stadiumRepository.save(stadium);
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getAddress(), stadium.getActive());
    }

    public StadiumResponseDTO updateStadium(Long id, @Valid StadiumUpdateDTO stadiumRequestDTO) {
        Stadium stadium = stadiumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estádio não encontrado!"));

        if (stadiumRequestDTO.name() != null && !stadiumRequestDTO.name().equalsIgnoreCase(stadium.getName())) {
            if (stadiumRepository.existsByNameIgnoreCase(stadiumRequestDTO.name())) {
                throw new ConflictException("Estádio já existe!");
            }
            stadium.setName(stadiumRequestDTO.name());
        }

        if (stadiumRequestDTO.cep() != null && !stadiumRequestDTO.cep().isBlank() &&
                (stadium.getAddress() == null || !stadiumRequestDTO.cep().replaceAll("-", "").equals(stadium.getAddress().getCep().replaceAll("-", "")))) {
            Address newAddress = viaCepService.findByCep(stadiumRequestDTO.cep().replace("-", ""));
            stadium.setAddress(newAddress);
        }
        stadiumRepository.save(stadium);
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getAddress(), stadium.getActive());
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
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getAddress(), stadium.getActive());
    }

    public Page<StadiumResponseDTO> searchStadiums(Pageable pageable) {
        return stadiumRepository.findAll(pageable)
                .map(StadiumResponseDTO::new);
    }

    public List<StadiumResponseDTO> findByCep(String cep) {

        List<Stadium> stadiums = stadiumRepository.findByAddressCep(cep);

        return stadiums.stream().map(StadiumResponseDTO::new).collect(Collectors.toList());
    }
}
