package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.dtos.response.ClubRankingDTO;
import com.neocamp.api_futebol.dtos.response.OppRetrospectDTO;
import com.neocamp.api_futebol.entities.Match;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
            AND (:routs IS NULL OR :routs = TRUE AND ABS(m.homeGoals - m.awayGoals) >= 3)
                AND (:side IS NULL OR (:side = 'casa' AND m.homeClub.id = :clubId) OR (:side = 'fora' AND m.awayClub.id = :clubId))
    """)
    Page<Match> findWithFilters(
            @Param("clubId") Long clubId,
            @Param("stadiumId") Long stadiumId,
            @Param("routs") Boolean routs,
            @Param("side") String side,
            Pageable pageable
    );

    @Query("""
    SELECT m FROM Match m
        WHERE m.homeClub.id = :id OR m.awayClub.id = :id
    """)
    List<Match> findAllMatchesForClub(Long id);

    @Query("""
    SELECT
        CASE WHEN m.homeClub.id = :clubId THEN m.awayClub.id ELSE m.homeClub.id END AS opponentId,
        CASE WHEN m.homeClub.id = :clubId THEN m.awayClub.name ELSE m.homeClub.name END AS opponentName,
        SUM(CASE WHEN
                ((m.homeClub.id = :clubId AND m.homeGoals > m.awayGoals)
                OR (m.awayClub.id = :clubId AND m.awayGoals > m.homeGoals))
            THEN 1 ELSE 0 END) AS victories,
        SUM(CASE WHEN m.homeGoals = m.awayGoals THEN 1 ELSE 0 END) AS draws,
        SUM(CASE WHEN
                ((m.homeClub.id = :clubId AND m.homeGoals < m.awayGoals)
                OR (m.awayClub.id = :clubId AND m.awayGoals < m.homeGoals))
            THEN 1 ELSE 0 END) AS defeats,
        SUM(CASE WHEN m.homeClub.id = :clubId THEN m.homeGoals ELSE m.awayGoals END) AS goalsFor,
        SUM(CASE WHEN m.homeClub.id = :clubId THEN m.awayGoals ELSE m.homeGoals END) AS goalsAgainst
    FROM Match m
    WHERE 
        (m.homeClub.id = :clubId OR m.awayClub.id = :clubId)
        AND (:side IS NULL OR (:side = 'casa' AND m.homeClub.id = :clubId) OR (:side = 'fora' AND m.awayClub.id = :clubId))
    GROUP BY
        CASE WHEN m.homeClub.id = :clubId THEN m.awayClub.id ELSE m.homeClub.id END,
        CASE WHEN m.homeClub.id = :clubId THEN m.awayClub.name ELSE m.homeClub.name END
    """)
    List<OppRetrospectDTO> findOppsStats(@Param("clubId") Long id, @Param("side") String side);

    @Query("""
    SELECT m FROM Match m
        WHERE (
            (m.homeClub.id = :id AND m.awayClub.id = :oppId)
            OR (m.awayClub.id = :id AND m.homeClub.id = :oppId)
                )
                AND (:side IS NULL OR (:side = 'casa' AND m.homeClub.id = :id) OR (:side = 'fora' AND m.awayClub.id = :id))
    """)
    List<Match> findAllMatchesBetweenClubs(Long id, Long oppId, String side);


    @Query("""
    SELECT new com.neocamp.api_futebol.dtos.response.ClubRankingDTO(
        c.id,
        c.name,
        SUM(
            CASE 
                WHEN (m.homeClub.id = c.id AND m.homeGoals > m.awayGoals) OR
                     (m.awayClub.id = c.id AND m.awayGoals > m.homeGoals)
                  THEN 3
                WHEN m.homeGoals = m.awayGoals AND (m.homeClub.id = c.id OR m.awayClub.id = c.id)
                  THEN 1
                ELSE 0
            END
        ),
        SUM(CASE WHEN m.homeClub.id = c.id THEN m.homeGoals ELSE m.awayGoals END),
        SUM(
            CASE 
                WHEN (m.homeClub.id = c.id AND m.homeGoals > m.awayGoals) OR
                     (m.awayClub.id = c.id AND m.awayGoals > m.homeGoals)
                  THEN 1 ELSE 0 
            END
        ),
        COUNT(m)
    )
    FROM Club c
    LEFT JOIN Match m ON m.homeClub.id = c.id OR m.awayClub.id = c.id
    GROUP BY c.id, c.name
""")
    List<ClubRankingDTO> findClubRanking();

    @Query("""
    SELECT m FROM Match m
        WHERE m.homeClub.id = :id
    """)
    List<Match> findAllHomeMatchesForClub(Long id);

    @Query("""
    SELECT m FROM Match m
        WHERE m.awayClub.id = :id
    """)
    List<Match> findAllAwayMatchesForClub(Long id);

    @Query("""
    SELECT COUNT(m) > 0 FROM Match m 
        WHERE (m.homeClub.id = :clubId OR m.awayClub.id = :clubId)
             AND m.matchDateTime < :date""")
    boolean existsMatchBeforeCreatedAt(@Param("clubId") Long clubId, @Param("date") java.time.LocalDateTime date);
}
