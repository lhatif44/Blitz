package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "career_stats")
@Getter
@Setter
//Represents one career-level stat for a player e.g. career_passing_yards = 52000
//One row per player per stat — recomputed whenever the ingestion pipeline updates season data
public class CareerStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //The player this career stat belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    //Stored directly to allow querying all stats for a position group without a join
    @Column(name = "position_group", nullable = false)
    private String positionGroup;

    //The name of the stat e.g. "career_passing_yards", "career_completion_pct"
    @Column(name = "stat_name", nullable = false)
    private String statName;

    //The computed career value for this stat
    @Column(name = "stat_value", nullable = false, precision = 12, scale = 3)
    private BigDecimal statValue;

    //Timestamp of when this value was last computed — updated on every recompute
    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
