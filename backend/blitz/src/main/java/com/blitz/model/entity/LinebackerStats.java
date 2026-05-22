package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "linebacker_stats")
@Getter
@Setter
public class LinebackerStats {

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

    @Column(name = "tackles_solo")
    private Integer tacklesSolo;

    @Column(name = "tackles_assist")
    private Integer tacklesAssist;

    @Column(name = "tackles_total")
    private Integer tacklesTotal;

    private BigDecimal sacks;
    private BigDecimal tfl;

    @Column(name = "qb_hits")
    private Integer qbHits;

    // coverage stats — how the LB performed when covering pass routes
    @Column(name = "targets_allowed")
    private Integer targetsAllowed;

    @Column(name = "receptions_allowed")
    private Integer receptionsAllowed;

    @Column(name = "yards_allowed")
    private Integer yardsAllowed;

    @Column(name = "tds_allowed")
    private Integer tdsAllowed;

    // passer rating when QB targets a receiver this LB is covering
    @Column(name = "passer_rating_allowed")
    private BigDecimal passerRatingAllowed;

    @Column(name = "completion_pct_allowed")
    private BigDecimal completionPctAllowed;

    // turnover stats
    private Integer interceptions;

    @Column(name = "int_yards")
    private Integer intYards;

    @Column(name = "int_tds")
    private Integer intTds;

    @Column(name = "passes_defended")
    private Integer passesDefended;

    @Column(name = "forced_fumbles")
    private Integer forcedFumbles;

    @Column(name = "fumble_recoveries")
    private Integer fumbleRecoveries;
}
