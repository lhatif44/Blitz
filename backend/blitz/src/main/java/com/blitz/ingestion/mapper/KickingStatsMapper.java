package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.KickingStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class KickingStatsMapper {

    public KickingStats map(CsvRow base, Player player, Team team, Integer season, String seasonType) {
        KickingStats stats = new KickingStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        stats.setFgMade(base.getInt("fg_made"));
        stats.setFgAttempted(base.getInt("fg_att"));
        stats.setFgPct(IngestionMath.fractionToPercent(base.getBigDecimal("fg_pct")));
        stats.setFgLong(base.getInt("fg_long"));

        int attempted1To19 = IngestionMath.orZero(IngestionMath.sum(base.getInt("fg_made_0_19"), base.getInt("fg_missed_0_19")));
        int attempted20To29 = IngestionMath.orZero(IngestionMath.sum(base.getInt("fg_made_20_29"), base.getInt("fg_missed_20_29")));
        int attempted30To39 = IngestionMath.orZero(IngestionMath.sum(base.getInt("fg_made_30_39"), base.getInt("fg_missed_30_39")));
        int attempted40To49 = IngestionMath.orZero(IngestionMath.sum(base.getInt("fg_made_40_49"), base.getInt("fg_missed_40_49")));
        int attempted50Plus = IngestionMath.orZero(IngestionMath.sum(
                IngestionMath.sum(base.getInt("fg_made_50_59"), base.getInt("fg_missed_50_59")),
                IngestionMath.sum(base.getInt("fg_made_60_"), base.getInt("fg_missed_60_"))));

        for (int blockedDistance : base.getIntList("fg_blocked_list")) {
            if (blockedDistance <= 19) {
                attempted1To19++;
            } else if (blockedDistance <= 29) {
                attempted20To29++;
            } else if (blockedDistance <= 39) {
                attempted30To39++;
            } else if (blockedDistance <= 49) {
                attempted40To49++;
            } else {
                attempted50Plus++;
            }
        }

        stats.setFg1To19Made(base.getInt("fg_made_0_19"));
        stats.setFg1To19Attempted(attempted1To19);
        stats.setFg20To29Made(base.getInt("fg_made_20_29"));
        stats.setFg20To29Attempted(attempted20To29);
        stats.setFg30To39Made(base.getInt("fg_made_30_39"));
        stats.setFg30To39Attempted(attempted30To39);
        stats.setFg40To49Made(base.getInt("fg_made_40_49"));
        stats.setFg40To49Attempted(attempted40To49);
        stats.setFg50PlusMade(IngestionMath.sum(base.getInt("fg_made_50_59"), base.getInt("fg_made_60_")));
        stats.setFg50PlusAttempted(attempted50Plus);

        stats.setXpMade(base.getInt("pat_made"));
        stats.setXpAttempted(base.getInt("pat_att"));
        stats.setXpPct(IngestionMath.fractionToPercent(base.getBigDecimal("pat_pct")));

        return stats;
    }
}
