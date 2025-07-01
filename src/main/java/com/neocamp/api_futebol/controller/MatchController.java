package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
public class MatchController {
    @Autowired
    private MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchesResponseDTO> createMatch(@RequestBody @Valid MatchesRequestDTO matchesRequestDTO) {
        MatchesResponseDTO matchesResponseDTO = matchService.createMatch(matchesRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(matchesResponseDTO);
    }
}
