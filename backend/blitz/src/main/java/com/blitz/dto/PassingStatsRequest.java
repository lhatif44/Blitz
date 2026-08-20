package com.blitz.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

// Request payload for POST /api/stats/passing. playerId/teamAbbr are resolved to entities by the
// controller rather than accepting nested Player/Team objects, so a caller can't set arbitrary
// fields on someone else's player/team record through this endpoint.
public record PassingStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer completions,
        @PositiveOrZero Integer attempts,
        @PositiveOrZero Integer passingYards,
        @PositiveOrZero Integer passingTds,
        @PositiveOrZero Integer interceptions,
        @PositiveOrZero Integer sacks,
        Integer sackYardsLost, // stored as a negative number (yards lost)

        @DecimalMin("0") @DecimalMax("100") BigDecimal completionPct,
        BigDecimal yardsPerAttempt,
        BigDecimal adjYardsPerAttempt,
        BigDecimal netYardsPerAttempt,
        @DecimalMin("0") @DecimalMax("100") BigDecimal tdPct,
        @DecimalMin("0") @DecimalMax("100") BigDecimal intPct,
        BigDecimal passerRating, // NFL passer rating scale is 0-158.3, not a percentage
        @DecimalMin("0") @DecimalMax("100") BigDecimal qbr,
        BigDecimal cpoe, // completion % over expected — can be negative
        BigDecimal epaPerDropback, // can be negative
        @DecimalMin("0") @DecimalMax("100") BigDecimal successRate,
        @DecimalMin("0") @DecimalMax("100") BigDecimal pressureRate,
        BigDecimal timeToThrowAvg,
        BigDecimal adot,
        @DecimalMin("0") @DecimalMax("100") BigDecimal onTargetPct,
        @DecimalMin("0") @DecimalMax("100") BigDecimal badThrowPct,
        @DecimalMin("0") @DecimalMax("100") BigDecimal blitzedPct,

        @PositiveOrZero Integer scrambles,
        Integer scrambleYards,
        Integer scrambleTds
) {
}
