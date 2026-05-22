package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "receiving_stats")
@Getter
@Setter
public class ReceivingStats {

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
    private Integer targets;
    private Integer receptions;

    @Column(name = "receiving_yards")
    private Integer receivingYards;

    @Column(name = "receiving_tds")
    private Integer receivingTds;

    private Integer drops;
    private Integer fumbles;

    // efficiency stats
    @Column(name = "catch_rate")
    private BigDecimal catchRate;

    @Column(name = "yards_per_target")
    private BigDecimal yardsPerTarget;

    @Column(name = "yards_per_reception")
    private BigDecimal yardsPerReception;

    @Column(name = "drop_rate")
    private BigDecimal dropRate;

    // average depth of target — how far downfield the player is typically targeted
    private BigDecimal adot;

    @Column(name = "air_yards")
    private Integer airYards;

    @Column(name = "yards_after_catch")
    private BigDecimal yardsAfterCatch;

    @Column(name = "yac_per_reception")
    private BigDecimal yacPerReception;

    // share of team targets this player received
    @Column(name = "target_share")
    private BigDecimal targetShare;

    // share of team air yards this player received
    @Column(name = "air_yards_share")
    private BigDecimal airYardsShare;

    // weighted opportunity rating — combines target share and air yards share
    private BigDecimal wopr;

    @Column(name = "epa_per_target")
    private BigDecimal epaPerTarget;

    // average separation from defender at time of target (from Next Gen Stats)
    @Column(name = "separation_avg")
    private BigDecimal separationAvg;

    @Column(name = "contested_catch_rate")
    private BigDecimal contestedCatchRate;

    @Column(name = "red_zone_targets")
    private Integer redZoneTargets;

    @Column(name = "end_zone_targets")
    private Integer endZoneTargets;
}
