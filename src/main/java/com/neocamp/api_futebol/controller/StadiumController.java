package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.dtos.response.StadiumResponseDTO;
import com.neocamp.api_futebol.services.StadiumService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stadiums")
public class StadiumController {
    @Autowired
    private StadiumService stadiumService;

    @PostMapping
    public ResponseEntity<StadiumResponseDTO> createMatch(@RequestBody @Valid StadiumRequestDTO stadiumRequestDTO){
        StadiumResponseDTO stadiumResponseDTO = stadiumService.createStadium(stadiumRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadiumResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StadiumResponseDTO>  updateStadium(@PathVariable Long id, @RequestBody @Valid StadiumRequestDTO stadiumRequestDTO){
        StadiumResponseDTO stadiumResponseDTO = stadiumService.updateStadium(id, stadiumRequestDTO);
        return ResponseEntity.ok().body(stadiumResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteStadium(@PathVariable Long id){
        stadiumService.deleteStadium(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StadiumResponseDTO> getStadium(@PathVariable Long id){
        StadiumResponseDTO stadiumResponseDTO = stadiumService.findById(id);
        return ResponseEntity.ok().body(stadiumResponseDTO);
    }
}
