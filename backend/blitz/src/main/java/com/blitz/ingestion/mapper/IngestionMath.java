package com.blitz.ingestion.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

// null-safe, divide-by-zero-safe arithmetic helpers shared by the stat mappers —
// used to compute rate stats (e.g. completion %) from raw counting stats
public final class IngestionMath {

    private IngestionMath() {
    }

    public static BigDecimal divide(Integer numerator, Integer denominator, int scale) {
        if (numerator == null || denominator == null || denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal numerator, Integer denominator, int scale) {
        if (numerator == null || denominator == null || denominator == 0) {
            return null;
        }
        return numerator.divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal percent(Integer numerator, Integer denominator, int scale) {
        BigDecimal ratio = divide(numerator, denominator, scale + 4);
        return ratio == null ? null : ratio.multiply(BigDecimal.valueOf(100)).setScale(scale, RoundingMode.HALF_UP);
    }

    // nflverse stores some rates as a 0-1 fraction ("fg_pct", "pat_pct") — our schema stores 0-100
    public static BigDecimal fractionToPercent(BigDecimal fraction) {
        return fraction == null ? null : fraction.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
    }

    public static Integer sum(Integer a, Integer b) {
        if (a == null && b == null) {
            return null;
        }
        return (a != null ? a : 0) + (b != null ? b : 0);
    }

    public static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
