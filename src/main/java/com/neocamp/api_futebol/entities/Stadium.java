package com.neocamp.api_futebol.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    public Stadium(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Stadium() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
