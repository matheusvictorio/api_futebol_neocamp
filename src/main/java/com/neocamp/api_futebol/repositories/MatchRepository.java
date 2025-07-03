package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    // Checa partida no estádio nesta data
    @Query(
            value = "SELECT * FROM matches m " +
                    "WHERE m.stadium_id = :stadiumId " +
                    "AND DATE(m.match_date_time) = DATE(:dateTime)",
            nativeQuery = true
    )
    List<Match> findByStadiumAndDay(
            @Param("stadiumId") Long stadiumId,
            @Param("dateTime") LocalDateTime dateTime
    );

    // Checa partidas próximas de um clube (+/- 48 horas)
    @Query(
            value = "SELECT * FROM matches m " +
                    "WHERE (m.home_club_id = :clubId OR m.away_club_id = :clubId) " +
                    "AND ABS(TIMESTAMPDIFF(HOUR, m.match_date_time, :dateTime)) < 48",
            nativeQuery = true
    )
    List<Match> findMatchesNearDateForClub(
            @Param("clubId") Long clubId,
            @Param("dateTime") LocalDateTime dateTime
    );
    @Query("""
        SELECT m FROM Match m
        WHERE (:clubId IS NULL OR m.homeClub.id = :clubId OR m.awayClub.id = :clubId)
          AND (:stadiumId IS NULL OR m.stadium.id = :stadiumId)
    """)
    Page<Match> findWithFilters(
            @Param("clubId") Long clubId,
            @Param("stadiumId") Long stadiumId,
            Pageable pageable
    );

    @Query("""
    SELECT m FROM Match m
        WHERE m.homeClub.id = :id OR m.awayClub.id = :id
    """)
    List<Match> findAllMatchesForClub(Long id);
}
