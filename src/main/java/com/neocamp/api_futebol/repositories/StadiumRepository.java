package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
}
