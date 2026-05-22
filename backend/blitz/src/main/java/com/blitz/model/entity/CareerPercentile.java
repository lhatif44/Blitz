package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "career_percentiles")
@Getter
@Setter
public class CareerPercentile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // the player this percentile belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // used to compare only within the same position group e.g. "QB", "CB"
    @Column(name = "position_group", nullable = false)
    private String positionGroup;

    // the stat being ranked e.g. "career_passing_yards", "career_sacks"
    @Column(name = "stat_name", nullable = false)
    private String statName;

    // the player's actual career value for this stat
    @Column(name = "stat_value")
    private BigDecimal statValue;

    // where this player ranks among all historical players at their position — 99.0 means top 1%
    private BigDecimal percentile;

    // timestamp of when this was last recalculated — refreshed weekly during ingestion
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
