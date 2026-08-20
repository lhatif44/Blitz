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

public record SecondaryStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer snaps,
        @PositiveOrZero Integer snapsInCoverage,
        @PositiveOrZero Integer tacklesSolo,
        @PositiveOrZero Integer tacklesAssist,
        @PositiveOrZero Integer tacklesTotal,

        @PositiveOrZero Integer targetsAllowed,
        @PositiveOrZero Integer receptionsAllowed,
        @PositiveOrZero Integer yardsAllowed,
        @PositiveOrZero Integer tdsAllowed,
        BigDecimal passerRatingAllowed, // NFL passer rating scale is 0-158.3, not a percentage
        @DecimalMin("0") @DecimalMax("100") BigDecimal completionPctAllowed,
        BigDecimal yardsPerTargetAllowed,
        @DecimalMin("0") @DecimalMax("100") BigDecimal burnedRate,

        @PositiveOrZero Integer interceptions,
        @PositiveOrZero Integer intYards,
        @PositiveOrZero Integer intReturnTds,
        @PositiveOrZero Integer passesDefended,
        @PositiveOrZero Integer forcedFumbles,

        @PositiveOrZero Integer runStops,
        @DecimalMin("0") @DecimalMax("100") BigDecimal runStopPct
) {
}
