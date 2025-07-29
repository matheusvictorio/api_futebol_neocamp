package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.response.ClubRankingDTO;
import com.neocamp.api_futebol.services.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final MatchService matchService;

    public RankingController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<ClubRankingDTO>> getRanking(@RequestParam(defaultValue = "pontos") String filter) {
        List<ClubRankingDTO> clubRankingDTOList = matchService.rankClubsByFilter(filter);
        return ResponseEntity.ok(clubRankingDTOList);
    }

    @GetMapping("/stream")
    public ResponseEntity<List<ClubRankingDTO>> getRankingStream(@RequestParam(defaultValue = "pontos") String filter) {
        List<ClubRankingDTO> clubRankingDTOList = matchService.rankClubsByFilterStream(filter);
        return ResponseEntity.ok(clubRankingDTOList);
    }

}
