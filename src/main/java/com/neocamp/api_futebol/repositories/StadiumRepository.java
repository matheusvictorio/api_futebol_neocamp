package com.neocamp.api_futebol.repositories;

import com.neocamp.api_futebol.entities.Stadium;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    boolean existsByNameIgnoreCase(@NotBlank(message = "Nome do estádio é obrigatório!") @Size(min = 3, message = "Nome deve ter pelo menos 2 letras!") String name);
}
