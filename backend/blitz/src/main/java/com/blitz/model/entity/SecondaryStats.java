package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "secondary_stats")
@Getter
@Setter
public class SecondaryStats {

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
    private Integer snaps;

    // snaps where this player was assigned to cover a receiver
    @Column(name = "snaps_in_coverage")
    private Integer snapsInCoverage;

    @Column(name = "tackles_solo")
    private Integer tacklesSolo;

    @Column(name = "tackles_assist")
    private Integer tacklesAssist;

    @Column(name = "tackles_total")
    private Integer tacklesTotal;

    // coverage stats — key metrics for CBs and safeties
    @Column(name = "targets_allowed")
    private Integer targetsAllowed;

    @Column(name = "receptions_allowed")
    private Integer receptionsAllowed;

    @Column(name = "yards_allowed")
    private Integer yardsAllowed;

    @Column(name = "tds_allowed")
    private Integer tdsAllowed;

    // passer rating when QB targets a receiver this player is covering — lower is better
    @Column(name = "passer_rating_allowed")
    private BigDecimal passerRatingAllowed;

    @Column(name = "completion_pct_allowed")
    private BigDecimal completionPctAllowed;

    @Column(name = "yards_per_target_allowed")
    private BigDecimal yardsPerTargetAllowed;

    // percentage of times the receiver caught the ball and gained significant yards
    @Column(name = "burned_rate")
    private BigDecimal burnedRate;

    // turnover stats
    private Integer interceptions;

    @Column(name = "int_yards")
    private Integer intYards;

    @Column(name = "int_return_tds")
    private Integer intReturnTds;

    @Column(name = "passes_defended")
    private Integer passesDefended;

    @Column(name = "forced_fumbles")
    private Integer forcedFumbles;

    // run support stats — more relevant for safeties than corners
    @Column(name = "run_stops")
    private Integer runStops;

    @Column(name = "run_stop_pct")
    private BigDecimal runStopPct;
}
