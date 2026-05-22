package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "kicking_stats")
@Getter
@Setter
public class KickingStats {

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

    // overall field goal totals
    @Column(name = "fg_made")
    private Integer fgMade;

    @Column(name = "fg_attempted")
    private Integer fgAttempted;

    @Column(name = "fg_pct")
    private BigDecimal fgPct;

    // field goals broken down by distance range
    @Column(name = "fg_1_19_made")
    private Integer fg1To19Made;

    @Column(name = "fg_1_19_attempted")
    private Integer fg1To19Attempted;

    @Column(name = "fg_20_29_made")
    private Integer fg20To29Made;

    @Column(name = "fg_20_29_attempted")
    private Integer fg20To29Attempted;

    @Column(name = "fg_30_39_made")
    private Integer fg30To39Made;

    @Column(name = "fg_30_39_attempted")
    private Integer fg30To39Attempted;

    @Column(name = "fg_40_49_made")
    private Integer fg40To49Made;

    @Column(name = "fg_40_49_attempted")
    private Integer fg40To49Attempted;

    @Column(name = "fg_50_plus_made")
    private Integer fg50PlusMade;

    @Column(name = "fg_50_plus_attempted")
    private Integer fg50PlusAttempted;

    // longest field goal made this season
    @Column(name = "fg_long")
    private Integer fgLong;

    // extra point stats
    @Column(name = "xp_made")
    private Integer xpMade;

    @Column(name = "xp_attempted")
    private Integer xpAttempted;

    @Column(name = "xp_pct")
    private BigDecimal xpPct;

    // kickoff stats
    private Integer kickoffs;
    private Integer touchbacks;

    @Column(name = "touchback_pct")
    private BigDecimal touchbackPct;
}
