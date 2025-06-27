package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.services.ClubService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clubs")
public class ClubController {
    @Autowired
    private ClubService clubService;

    @PostMapping
    public ResponseEntity<ClubsResponseDTO> createClub(@RequestBody @Valid ClubsRequestDTO clubsRequestDTO) {
        var clubsResponseDTO = clubService.createClub(clubsRequestDTO);
        //devo usar URI ao inves do status?
        return ResponseEntity.status(HttpStatus.CREATED).body(clubsResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubsResponseDTO> updateClub(@PathVariable Long id, @RequestBody @Valid ClubsRequestDTO clubsRequestDTO) {
        var clubsResponseDTO = clubService.updateClub(id, clubsRequestDTO);
        return ResponseEntity.ok().body(clubsResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubsResponseDTO> getClub(@PathVariable Long id) {
        ClubsResponseDTO clubsResponseDTO = clubService.findById(id);
        return ResponseEntity.ok().body(clubsResponseDTO);
    }
}
