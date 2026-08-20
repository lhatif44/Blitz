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

public record PassRushStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer snaps,
        @PositiveOrZero Integer tacklesSolo,
        @PositiveOrZero Integer tacklesAssist,
        @PositiveOrZero Integer tacklesTotal,
        @PositiveOrZero BigDecimal sacks, // stored as decimal to allow half-sacks e.g. 4.5
        @PositiveOrZero BigDecimal tfl,
        @PositiveOrZero Integer qbHits,
        @PositiveOrZero Integer qbHurries,
        @PositiveOrZero Integer pressures,

        @DecimalMin("0") @DecimalMax("100") BigDecimal passRushWinRate,
        @DecimalMin("0") @DecimalMax("100") BigDecimal pressureRate,
        @DecimalMin("0") @DecimalMax("100") BigDecimal sackPct,

        @PositiveOrZero Integer runStops,
        @DecimalMin("0") @DecimalMax("100") BigDecimal runStopPct,
        @PositiveOrZero Integer stuffs,

        @PositiveOrZero Integer forcedFumbles,
        @PositiveOrZero Integer fumbleRecoveries,
        @PositiveOrZero Integer fumbleReturnTds
) {
}
