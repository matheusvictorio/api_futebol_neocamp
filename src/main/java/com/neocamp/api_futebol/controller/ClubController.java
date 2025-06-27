package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.services.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clubs")
public class ClubController {
    @Autowired
    private ClubService clubService;

    @PostMapping
    public ResponseEntity<ClubsResponseDTO> createClub(@RequestBody ClubsRequestDTO clubsRequestDTO) {
        ClubsResponseDTO clubsResponseDTO = clubService.createClub(clubsRequestDTO);
        //devo usar URI ao inves do status?
        return ResponseEntity.status(HttpStatus.CREATED).body(clubsResponseDTO);
    }
}
