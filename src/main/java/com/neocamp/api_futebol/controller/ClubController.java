package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.ClubsRequestDTO;
import com.neocamp.api_futebol.dtos.response.ClubsResponseDTO;
import com.neocamp.api_futebol.dtos.response.MatchesRetrospectDTO;
import com.neocamp.api_futebol.dtos.response.OppRetrospectDTO;
import com.neocamp.api_futebol.entities.State;
import com.neocamp.api_futebol.services.ClubService;
import com.neocamp.api_futebol.services.MatchService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
public class ClubController {
    @Autowired
    private ClubService clubService;
    @Autowired
    private MatchService matchService;

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

    @GetMapping
    public ResponseEntity<Page<ClubsResponseDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) State state,
            @RequestParam(required = false) Boolean active,
            @ParameterObject
            @PageableDefault(size =  10, page = 0)
            Pageable pageable
    ) {
        Page<ClubsResponseDTO> page = clubService.searchClubs(name, state, active, pageable);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/{id}/retrospect")
    public ResponseEntity<MatchesRetrospectDTO> getClubRetrospective(@PathVariable Long id, @RequestParam(required = false) String side) {
        var matchesRetrospectDTO = matchService.getClubRetrospective(id, side);
        return ResponseEntity.ok().body(matchesRetrospectDTO);
    }

    @GetMapping("/{id}/opp/retrospect")
    public ResponseEntity<List<OppRetrospectDTO>> getClubOppRetrospective(@PathVariable Long id, @RequestParam(required = false) String side) {
        List<OppRetrospectDTO> oppRetrospectDTOList = matchService.getOppRetrospects(id, side);
        return ResponseEntity.ok().body(oppRetrospectDTOList);
    }

    @GetMapping("/{id}/opp/{oppId}/retrospect")
    public ResponseEntity<OppRetrospectDTO> getClubOneOppRetrospective(@PathVariable Long id, @PathVariable Long oppId, @RequestParam(required = false) String side) {
        var oneOppRetrospectDTO = matchService.getOneOppRestrospect(id, oppId, side);
        return ResponseEntity.ok().body(oneOppRetrospectDTO);
    }
}
