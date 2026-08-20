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

public record KickingStatsRequest(
        UUID id,
        @NotNull UUID playerId,
        String teamAbbr,
        @NotNull @Min(1920) @Max(2100) Integer season,
        @NotBlank @Pattern(regexp = "REG|POST") String seasonType,

        @PositiveOrZero Integer games,

        @PositiveOrZero Integer fgMade,
        @PositiveOrZero Integer fgAttempted,
        @DecimalMin("0") @DecimalMax("100") BigDecimal fgPct,

        @PositiveOrZero Integer fg1To19Made,
        @PositiveOrZero Integer fg1To19Attempted,
        @PositiveOrZero Integer fg20To29Made,
        @PositiveOrZero Integer fg20To29Attempted,
        @PositiveOrZero Integer fg30To39Made,
        @PositiveOrZero Integer fg30To39Attempted,
        @PositiveOrZero Integer fg40To49Made,
        @PositiveOrZero Integer fg40To49Attempted,
        @PositiveOrZero Integer fg50PlusMade,
        @PositiveOrZero Integer fg50PlusAttempted,
        @PositiveOrZero Integer fgLong,

        @PositiveOrZero Integer xpMade,
        @PositiveOrZero Integer xpAttempted,
        @DecimalMin("0") @DecimalMax("100") BigDecimal xpPct,

        @PositiveOrZero Integer kickoffs,
        @PositiveOrZero Integer touchbacks,
        @DecimalMin("0") @DecimalMax("100") BigDecimal touchbackPct
) {
}
