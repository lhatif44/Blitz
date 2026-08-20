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

public record RushingStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer carries,
        @PositiveOrZero Integer rushingYards,
        @PositiveOrZero Integer rushingTds,
        @PositiveOrZero Integer fumbles,
        @PositiveOrZero Integer fumblesLost,
        @PositiveOrZero Integer rushingFirstDowns,

        BigDecimal yardsPerCarry,
        @DecimalMin("0") @DecimalMax("100") BigDecimal successRate,
        BigDecimal rushingEpa, // can be negative

        BigDecimal yardsAfterContact,
        @PositiveOrZero Integer brokenTackles,
        BigDecimal yacPerCarry,
        @DecimalMin("0") @DecimalMax("100") BigDecimal stuffRate,
        @PositiveOrZero Integer runs10Plus,
        @PositiveOrZero Integer runs20Plus,

        @PositiveOrZero Integer targets,
        @PositiveOrZero Integer receptions,
        @PositiveOrZero Integer receivingYards,
        @PositiveOrZero Integer receivingTds,
        BigDecimal yardsPerReception
) {
}
