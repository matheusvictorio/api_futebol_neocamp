package com.neocamp.api_futebol.services;


import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.State;
import com.neocamp.api_futebol.repositories.ClubRepository;
import com.neocamp.api_futebol.exception.NotFoundException;
import com.neocamp.api_futebol.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class ClubService {
    @Autowired
    private ClubRepository clubRepository;

    public ClubsResponseDTO createClub(ClubsRequestDTO dto) {
        //validação se existe no com o mesmo nome no mesmo estado
        validarConflitoNomeEstado(dto.name(), dto.state(), null);
        Club club = new Club(dto.name(), dto.state(), dto.createdAt());
        Club savedUser = clubRepository.save(club);
        return new ClubsResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getState(), savedUser.getActive(), savedUser.getCreatedAt());
    }

    public ClubsResponseDTO updateClub(Long id, @Valid ClubsRequestDTO dto) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado"));
        validarConflitoNomeEstado(dto.name(), dto.state(), id);
        club.setName(dto.name());
        club.setState(dto.state());
        club.setCreatedAt(dto.createdAt());
        club = clubRepository.save(club);
        return new ClubsResponseDTO(club.getId(), club.getName(), club.getState(), club.getActive(), club.getCreatedAt());
    }

    private void validarConflitoNomeEstado(String name, State state, Long currentId) {
        Optional<Club> clubOpt = clubRepository.findByNameIgnoreCaseAndState(name, state);
        if (clubOpt.isPresent() && !clubOpt.get().getId().equals(currentId)) {
            throw new BadRequestException("Já existe outro clube com esse nome e estado");
        }
    }

    public void deleteClub(Long id) {
        var club = clubRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado"));
        if (Boolean.FALSE.equals(club.getActive())) {
            throw new BadRequestException("Clube já está inativo!");
        }
        club.delete();
        clubRepository.save(club);
    }

    public ClubsResponseDTO findById(Long id) {
        var club =  clubRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Clube não encontrado ou inativo"));;
        return new ClubsResponseDTO(club.getId(), club.getName(), club.getState(), club.getActive(), club.getCreatedAt());
    }


    public Page<ClubsResponseDTO> searchClubs(String name, String state, Boolean active, Pageable pageable) {
        return clubRepository.findWithFilter(name, state, active, pageable)
                .map(ClubsResponseDTO::new);

    }
}
