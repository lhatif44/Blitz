package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rushing_stats")
@Getter
@Setter
public class RushingStats {

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

    // volume stats
    private Integer games;
    private Integer carries;

    @Column(name = "rushing_yards")
    private Integer rushingYards;

    @Column(name = "rushing_tds")
    private Integer rushingTds;

    private Integer fumbles;

    @Column(name = "fumbles_lost")
    private Integer fumblesLost;

    @Column(name = "rushing_first_downs")
    private Integer rushingFirstDowns;

    // efficiency stats
    @Column(name = "yards_per_carry")
    private BigDecimal yardsPerCarry;

    @Column(name = "success_rate")
    private BigDecimal successRate;

    @Column(name = "rushing_epa")
    private BigDecimal rushingEpa;

    // advanced metrics
    @Column(name = "yards_after_contact")
    private BigDecimal yardsAfterContact;

    @Column(name = "broken_tackles")
    private Integer brokenTackles;

    @Column(name = "yac_per_carry")
    private BigDecimal yacPerCarry;

    // percentage of runs stopped at or behind the line of scrimmage
    @Column(name = "stuff_rate")
    private BigDecimal stuffRate;

    @Column(name = "runs_10_plus")
    private Integer runs10Plus;

    @Column(name = "runs_20_plus")
    private Integer runs20Plus;

    // receiving stats — RBs often catch passes out of the backfield
    private Integer targets;
    private Integer receptions;

    @Column(name = "receiving_yards")
    private Integer receivingYards;

    @Column(name = "receiving_tds")
    private Integer receivingTds;

    @Column(name = "yards_per_reception")
    private BigDecimal yardsPerReception;
}
