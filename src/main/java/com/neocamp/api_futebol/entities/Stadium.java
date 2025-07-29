package com.neocamp.api_futebol.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Boolean active = true;
    @Embedded
    private Address address;


    public Stadium(String name) {
        this.name = name;
        this.active = true;
        this.address = new Address();
    }

    public Stadium() {}

    public Long getId() {
        return id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public void delete(){
        this.active = false;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setId(long l) {

    }
}
