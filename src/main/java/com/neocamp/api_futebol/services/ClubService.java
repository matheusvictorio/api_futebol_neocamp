package com.neocamp.api_futebol.services;


import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.repositories.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClubService {
    @Autowired
    private ClubRepository clubRepository;

    public ClubsResponseDTO createClub(ClubsRequestDTO dto) {
        //validação se existe no com o mesmo nome no mesmo estado
        boolean exist = clubRepository.existsByNameIgnoreCaseAndState(dto.name(), dto.state());
        if (exist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um clube com esse nome nesse estado!");
        }

        //pego as informações passadas e transformo em um Club
        Club club = new Club(dto.name(), dto.state(), dto.createdAt());
        //Salvo o Clube e tranformo em uma variável para retornar
        Club savedUser = clubRepository.save(club);

        return new ClubsResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getState(), savedUser.getActive());
    }


}
