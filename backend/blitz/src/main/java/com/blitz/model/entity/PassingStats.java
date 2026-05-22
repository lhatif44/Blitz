package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "passing_stats")
@Getter
@Setter
public class PassingStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // many stat lines can belong to one player
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_abbr")
    private Team team;

    private Integer season;

    // "REG" for regular season, "POST" for playoffs
    @Column(name = "season_type")
    private String seasonType;

    // volume stats
    private Integer games;
    private Integer completions;
    private Integer attempts;

    @Column(name = "passing_yards")
    private Integer passingYards;

    @Column(name = "passing_tds")
    private Integer passingTds;

    private Integer interceptions;
    private Integer sacks;

    @Column(name = "sack_yards_lost")
    private Integer sackYardsLost;

    // efficiency stats
    @Column(name = "completion_pct")
    private BigDecimal completionPct;

    @Column(name = "yards_per_attempt")
    private BigDecimal yardsPerAttempt;

    @Column(name = "adj_yards_per_attempt")
    private BigDecimal adjYardsPerAttempt;

    @Column(name = "net_yards_per_attempt")
    private BigDecimal netYardsPerAttempt;

    @Column(name = "td_pct")
    private BigDecimal tdPct;

    @Column(name = "int_pct")
    private BigDecimal intPct;

    @Column(name = "passer_rating")
    private BigDecimal passerRating;

    // ESPN's total QBR metric
    private BigDecimal qbr;

    // advanced metrics from nflfastR
    private BigDecimal cpoe; // completion percentage over expected

    @Column(name = "epa_per_dropback")
    private BigDecimal epaPerDropback; // expected points added per dropback

    @Column(name = "success_rate")
    private BigDecimal successRate;

    @Column(name = "pressure_rate")
    private BigDecimal pressureRate;

    @Column(name = "time_to_throw_avg")
    private BigDecimal timeToThrowAvg;

    // average depth of target
    private BigDecimal adot;

    @Column(name = "on_target_pct")
    private BigDecimal onTargetPct;

    @Column(name = "bad_throw_pct")
    private BigDecimal badThrowPct;

    @Column(name = "blitzed_pct")
    private BigDecimal blitzedPct;

    // rushing stats for mobile QBs
    private Integer scrambles;

    @Column(name = "scramble_yards")
    private Integer scrambleYards;

    @Column(name = "scramble_tds")
    private Integer scrambleTds;
}
