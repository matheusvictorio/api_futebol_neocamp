package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByNameIgnoreCaseAndState(String name, State state);


    Optional<Club> findByIdAndActiveTrue(Long id);

    @Query("""
            SELECT c FROM Club c
    WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:state IS NULL OR c.state = :state)
    AND (:active IS NULL OR c.active = :active)
    """
    )
    Page<Club> findWithFilter(String name, State state, Boolean active, Pageable pageable);
}
