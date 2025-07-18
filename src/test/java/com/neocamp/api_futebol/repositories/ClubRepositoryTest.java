package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class ClubRepositoryTest {
    @Autowired
    private ClubRepository clubRepository;

    @Test
    @DisplayName("should find clubs with all filters")
    void findWithFilterCase1() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        club1.setActive(true);
        clubRepository.save(club1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter("Flamengo", State.RJ, Boolean.TRUE, pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("Flamengo", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("should find clubs with only name filter")
    void findWithFilterCase2() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        Club club2 = new Club("Flamengo", State.SP, LocalDate.now());
        clubRepository.saveAll(List.of(club1, club2));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter("Flamengo", null, null, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should find clubs with only state filter")
    void findWithFilterCase3() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        Club club2 = new Club("Vasco", State.RJ, LocalDate.now());
        clubRepository.saveAll(List.of(club1, club2));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter(null, State.RJ, null, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should find clubs with only active filter")
    void findWithFilterCase4() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        Club club2 = new Club("Palmeiras", State.SP, LocalDate.now());
        club1.setActive(true);
        club2.setActive(true);
        clubRepository.saveAll(List.of(club1, club2));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter(null, null, true, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should find clubs with name and state filter")
    void findWithFilterCase5() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        clubRepository.save(club1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter("Flamengo", State.RJ, null, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find clubs with name and active filter")
    void findWithFilterCase6() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        club1.setActive(true);
        clubRepository.save(club1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter("Flamengo", null, true, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("should find clubs with state and active filter")
    void findWithFilterCase7() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        Club club2 = new Club("Vasco", State.RJ, LocalDate.now());
        club1.setActive(true);
        club2.setActive(true);
        clubRepository.saveAll(List.of(club1, club2));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter(null, State.RJ, true, pageable);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("should return all clubs when all filters are null")
    void findWithFilterCase8() {
        Club club1 = new Club("Flamengo", State.RJ, LocalDate.now());
        Club club2 = new Club("Palmeiras", State.SP, LocalDate.now());
        Club club3 = new Club("Vasco", State.RJ, LocalDate.now());
        clubRepository.saveAll(List.of(club1, club2, club3));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter(null, null, null, pageable);
        assertEquals(3, result.getTotalElements());
    }

    @Test
    @DisplayName("should no return results")
    void findWithFilteCase9() {
        Club club1 = new Club("Flamengo", State.PB, LocalDate.now());
        club1.setActive(true);
        clubRepository.save(club1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> result = clubRepository.findWithFilter("Nonexistent", State.RJ, false, pageable);
        assertEquals(0, result.getTotalElements());
    }
}
