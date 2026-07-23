package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.PassingStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PassingStatsMapper {

    public PassingStats map(CsvRow base, CsvRow advRow, Player player, Team team, Integer season, String seasonType) {
        PassingStats stats = new PassingStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        Integer completions = base.getInt("completions");
        Integer attempts = base.getInt("attempts");
        Integer passingYards = base.getInt("passing_yards");
        Integer passingTds = base.getInt("passing_tds");
        Integer interceptions = base.getInt("passing_interceptions");
        Integer sacks = base.getInt("sacks_suffered");
        Integer sackYardsLost = base.getInt("sack_yards_lost");

        stats.setCompletions(completions);
        stats.setAttempts(attempts);
        stats.setPassingYards(passingYards);
        stats.setPassingTds(passingTds);
        stats.setInterceptions(interceptions);
        stats.setSacks(sacks);
        stats.setSackYardsLost(sackYardsLost);

        stats.setCompletionPct(IngestionMath.percent(completions, attempts, 1));
        stats.setYardsPerAttempt(IngestionMath.divide(passingYards, attempts, 2));
        stats.setTdPct(IngestionMath.percent(passingTds, attempts, 1));
        stats.setIntPct(IngestionMath.percent(interceptions, attempts, 1));
        stats.setPasserRating(passerRating(completions, attempts, passingYards, passingTds, interceptions));
        stats.setAdjYardsPerAttempt(adjYardsPerAttempt(passingYards, passingTds, interceptions, attempts));
        stats.setNetYardsPerAttempt(netYardsPerAttempt(passingYards, sackYardsLost, attempts, sacks));

        BigDecimal passingEpa = base.getBigDecimal("passing_epa");
        int dropbacks = (attempts != null ? attempts : 0) + (sacks != null ? sacks : 0);
        stats.setEpaPerDropback(dropbacks > 0 && passingEpa != null
                ? passingEpa.divide(BigDecimal.valueOf(dropbacks), 4, RoundingMode.HALF_UP)
                : null);
        stats.setCpoe(base.getBigDecimal("passing_cpoe"));

        if (advRow != null) {
            stats.setPressureRate(advRow.getBigDecimal("pressure_pct"));
            stats.setTimeToThrowAvg(advRow.getBigDecimal("pocket_time"));
            stats.setOnTargetPct(advRow.getBigDecimal("on_tgt_pct"));
            stats.setBadThrowPct(advRow.getBigDecimal("bad_throw_pct"));
            stats.setBlitzedPct(IngestionMath.percent(advRow.getInt("times_blitzed"), advRow.getInt("pass_attempts"), 1));
            stats.setScrambles(advRow.getInt("scrambles"));
        }

        return stats;
    }

    private static BigDecimal clamp(BigDecimal value) {
        BigDecimal max = new BigDecimal("2.375");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return value.compareTo(max) > 0 ? max : value;
    }

    private static BigDecimal passerRating(Integer completions, Integer attempts, Integer yards, Integer tds, Integer ints) {
        if (completions == null || attempts == null || yards == null || tds == null || ints == null || attempts == 0) {
            return null;
        }
        BigDecimal att = BigDecimal.valueOf(attempts);
        BigDecimal a = clamp(BigDecimal.valueOf(completions).divide(att, 6, RoundingMode.HALF_UP)
                .subtract(new BigDecimal("0.3")).multiply(BigDecimal.valueOf(5)));
        BigDecimal b = clamp(BigDecimal.valueOf(yards).divide(att, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(3)).multiply(new BigDecimal("0.25")));
        BigDecimal c = clamp(BigDecimal.valueOf(tds).divide(att, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(20)));
        BigDecimal d = clamp(new BigDecimal("2.375")
                .subtract(BigDecimal.valueOf(ints).divide(att, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(25))));
        return a.add(b).add(c).add(d)
                .divide(BigDecimal.valueOf(6), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal adjYardsPerAttempt(Integer yards, Integer tds, Integer ints, Integer attempts) {
        if (yards == null || tds == null || ints == null || attempts == null || attempts == 0) {
            return null;
        }
        BigDecimal numerator = BigDecimal.valueOf(yards)
                .add(BigDecimal.valueOf(tds).multiply(BigDecimal.valueOf(20)))
                .subtract(BigDecimal.valueOf(ints).multiply(BigDecimal.valueOf(45)));
        return numerator.divide(BigDecimal.valueOf(attempts), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal netYardsPerAttempt(Integer yards, Integer sackYardsLost, Integer attempts, Integer sacks) {
        if (yards == null || attempts == null) {
            return null;
        }
        int denominator = attempts + (sacks != null ? sacks : 0);
        if (denominator == 0) {
            return null;
        }
        BigDecimal numerator = BigDecimal.valueOf(yards).subtract(BigDecimal.valueOf(sackYardsLost != null ? sackYardsLost : 0));
        return numerator.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
