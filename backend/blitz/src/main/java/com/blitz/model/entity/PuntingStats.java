package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "punting_stats")
@Getter
@Setter
public class PuntingStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_abbr")
    private Team team;

    private Integer season;

    @Column(name = "season_type")
    private String seasonType;

    private Integer games;
    private Integer punts;

    @Column(name = "punt_yards")
    private Integer puntYards;

    // total yards divided by number of punts
    @Column(name = "gross_avg")
    private BigDecimal grossAvg;

    // gross avg minus return yards — better measure of punter effectiveness
    @Column(name = "net_avg")
    private BigDecimal netAvg;

    // punts that landed inside the opponent's 20 yard line without going into the end zone
    @Column(name = "punts_inside_20")
    private Integer puntsInside20;

    // punts that went into or through the end zone for a touchback
    private Integer touchbacks;

    @Column(name = "long_punt")
    private Integer longPunt;

    @Column(name = "fair_catches_forced")
    private Integer fairCatchesForced;
}
