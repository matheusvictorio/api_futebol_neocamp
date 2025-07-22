package com.neocamp.api_futebol.controller;

import com.neocamp.api_futebol.dtos.request.StadiumRequestDTO;
import com.neocamp.api_futebol.dtos.request.StadiumUpdateDTO;
import com.neocamp.api_futebol.dtos.response.StadiumResponseDTO;
import com.neocamp.api_futebol.services.StadiumService;
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
@RequestMapping("/stadiums")
public class StadiumController {
    @Autowired
    private StadiumService stadiumService;

    @PostMapping
    public ResponseEntity<StadiumResponseDTO> createStadium(@RequestBody @Valid StadiumRequestDTO stadiumRequestDTO){
        StadiumResponseDTO stadiumResponseDTO = stadiumService.createStadium(stadiumRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadiumResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StadiumResponseDTO>  updateStadium(@PathVariable Long id, @RequestBody @Valid StadiumUpdateDTO stadiumRequestDTO){
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

    @GetMapping
    public ResponseEntity<Page<StadiumResponseDTO>> getStadiums(
            @ParameterObject
            @PageableDefault(size =  10, page = 0)
            Pageable pageable){
        Page<StadiumResponseDTO> page = stadiumService.searchStadiums(pageable);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/by-cep/{cep}")
    public ResponseEntity<List<StadiumResponseDTO>> getStadiumByCep(@PathVariable String cep){
        List<StadiumResponseDTO> stadiumResponseDTO = stadiumService.findByCep(cep);
        return ResponseEntity.ok().body(stadiumResponseDTO);
    }
}
