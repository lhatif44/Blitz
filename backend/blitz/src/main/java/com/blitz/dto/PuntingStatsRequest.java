package com.blitz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record PuntingStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,
        @PositiveOrZero Integer punts,
        @PositiveOrZero Integer puntYards,
        BigDecimal grossAvg,
        BigDecimal netAvg,
        @PositiveOrZero Integer puntsInside20,
        @PositiveOrZero Integer touchbacks,
        @PositiveOrZero Integer longPunt,
        @PositiveOrZero Integer fairCatchesForced
) {
}
