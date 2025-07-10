package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.MatchesRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesResponseDTO;
import com.neocamp.api_futebol.repositories.MatchRepository;
import com.neocamp.api_futebol.services.ClubService;
import com.neocamp.api_futebol.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
public class MatchController {
    @Autowired
    private MatchService matchService;
    @Autowired
    private ClubService clubService;
    @Autowired
    private MatchRepository matchRepository;

    @PostMapping
    public ResponseEntity<MatchesResponseDTO> createMatch(@RequestBody @Valid MatchesRequestDTO matchesRequestDTO) {
        MatchesResponseDTO matchesResponseDTO = matchService.createMatch(matchesRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(matchesResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchesResponseDTO> updateMatch(@PathVariable Long id, @RequestBody MatchesRequestDTO matchesRequestDTO) {
        MatchesResponseDTO matchesResponseDTO = matchService.updateMatch(id, matchesRequestDTO);
        return ResponseEntity.ok().body(matchesResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteMatch(@PathVariable Long id) {
        matchService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchesResponseDTO> getMatch(@PathVariable Long id) {
        MatchesResponseDTO matchesResponseDTO = matchService.findById(id);
        return ResponseEntity.ok().body(matchesResponseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<MatchesResponseDTO>> getAll(
            @RequestParam(required = false) Long clubId,
            @RequestParam(required = false) Long stadiumId,
            @RequestParam(required = false) Boolean routs,
            Pageable pageable
    ){
        Page<MatchesResponseDTO> page = matchService.searchMatches(clubId, stadiumId, routs, pageable);
        return ResponseEntity.ok().body(page);
    }
}
