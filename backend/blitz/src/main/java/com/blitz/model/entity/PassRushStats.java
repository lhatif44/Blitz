package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pass_rush_stats")
@Getter
@Setter
public class PassRushStats {

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

    // sacks stored as decimal to account for half sacks e.g. 4.5
    private BigDecimal sacks;

    // tackles for loss
    private BigDecimal tfl;

    @Column(name = "qb_hits")
    private Integer qbHits;

    @Column(name = "qb_hurries")
    private Integer qbHurries;

    private Integer pressures;

    // efficiency stats
    @Column(name = "pass_rush_win_rate")
    private BigDecimal passRushWinRate;

    @Column(name = "pressure_rate")
    private BigDecimal pressureRate;

    @Column(name = "sack_pct")
    private BigDecimal sackPct;

    // run defense stats
    @Column(name = "run_stops")
    private Integer runStops;

    @Column(name = "run_stop_pct")
    private BigDecimal runStopPct;

    // runs stopped at or behind the line of scrimmage
    private Integer stuffs;

    // turnover stats
    @Column(name = "forced_fumbles")
    private Integer forcedFumbles;

    @Column(name = "fumble_recoveries")
    private Integer fumbleRecoveries;

    @Column(name = "fumble_return_tds")
    private Integer fumbleReturnTds;
}
