package com.neocamp.api_futebol.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Club homeClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Club awayClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Stadium stadium;

    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    @Column(nullable = false)
    private Integer homeGoals;

    @Column(nullable = false)
    private Integer awayGoals;


    public Match() {
    }

    public Long getId() {
        return id;
    }



    public Club getHomeClub() {
        return homeClub;
    }

    public void setHomeClub(Club homeClub) {
        this.homeClub = homeClub;
    }

    public Club getAwayClub() {
        return awayClub;
    }

    public void setAwayClub(Club awayClub) {
        this.awayClub = awayClub;
    }

    public Stadium getStadium() {
        return stadium;
    }

    public void setStadium(Stadium stadium) {
        this.stadium = stadium;
    }

    public LocalDateTime getMatchDateTime() {
        return matchDateTime;
    }

    public void setMatchDateTime(LocalDateTime matchDateTime) {
        this.matchDateTime = matchDateTime;
    }

    public Integer getHomeGoals() {
        return homeGoals;
    }

    public void setHomeGoals(Integer homeGoals) {
        this.homeGoals = homeGoals;
    }

    public Integer getAwayGoals() {
        return awayGoals;
    }

    public void setAwayGoals(Integer awayGoals) {
        this.awayGoals = awayGoals;
    }

    public Match(Club homeClub, Club awayClub, Stadium stadium, LocalDateTime matchDateTime, Integer homeGoals, Integer awayGoals) {
        this.homeClub = homeClub;
        this.awayClub = awayClub;
        this.stadium = stadium;
        this.matchDateTime = matchDateTime;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }
}
