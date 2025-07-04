package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.response.ClubRankingDTO;
import com.neocamp.api_futebol.services.MatchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingController {
    @Autowired
    private MatchService matchService;

    @GetMapping
    public ResponseEntity<List<ClubRankingDTO>> getRanking(@RequestParam String filter) {
        List<ClubRankingDTO> clubRankingDTOList = matchService.rankClubsByFilter(filter);
        return ResponseEntity.ok(clubRankingDTOList);
    }

}
