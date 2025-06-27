package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Club;
import com.neocamp.api_futebol.entities.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
    //boolean existsByNameIgnoreCaseAndState(@NotBlank(message = "Nome do clube é obrigatório!") @Size(min = 2, message = "Nome deve ter pelo menos 2 letras") String name, @NotNull(message = "Estado é obrigatório!") State state);

    Optional<Club> findByNameIgnoreCaseAndState(String name, State state);
}
