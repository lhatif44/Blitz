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

public record ReceivingStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer targets,
        @PositiveOrZero Integer receptions,
        @PositiveOrZero Integer receivingYards,
        @PositiveOrZero Integer receivingTds,
        @PositiveOrZero Integer drops,
        @PositiveOrZero Integer fumbles,

        @DecimalMin("0") @DecimalMax("100") BigDecimal catchRate,
        BigDecimal yardsPerTarget,
        BigDecimal yardsPerReception,
        @DecimalMin("0") @DecimalMax("100") BigDecimal dropRate,
        BigDecimal adot,
        @PositiveOrZero Integer airYards,
        BigDecimal yardsAfterCatch,
        BigDecimal yacPerReception,
        @DecimalMin("0") @DecimalMax("100") BigDecimal targetShare,
        @DecimalMin("0") @DecimalMax("100") BigDecimal airYardsShare,
        BigDecimal wopr,
        BigDecimal epaPerTarget, // can be negative
        BigDecimal separationAvg,
        @DecimalMin("0") @DecimalMax("100") BigDecimal contestedCatchRate,
        @PositiveOrZero Integer redZoneTargets,
        @PositiveOrZero Integer endZoneTargets
) {
}
