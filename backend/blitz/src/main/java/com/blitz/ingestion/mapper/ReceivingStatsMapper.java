package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.ReceivingStats;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class ReceivingStatsMapper {

    public ReceivingStats map(CsvRow base, CsvRow advRow, Player player, Team team, Integer season, String seasonType) {
        ReceivingStats stats = new ReceivingStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        Integer targets = base.getInt("targets");
        Integer receptions = base.getInt("receptions");
        Integer receivingYards = base.getInt("receiving_yards");
        stats.setTargets(targets);
        stats.setReceptions(receptions);
        stats.setReceivingYards(receivingYards);
        stats.setReceivingTds(base.getInt("receiving_tds"));
        stats.setFumbles(base.getInt("receiving_fumbles"));

        stats.setCatchRate(IngestionMath.percent(receptions, targets, 1));
        stats.setYardsPerTarget(IngestionMath.divide(receivingYards, targets, 2));
        stats.setYardsPerReception(IngestionMath.divide(receivingYards, receptions, 2));

        stats.setAirYards(base.getInt("receiving_air_yards"));
        stats.setYardsAfterCatch(base.getBigDecimal("receiving_yards_after_catch"));
        stats.setTargetShare(base.getBigDecimal("target_share"));
        stats.setAirYardsShare(base.getBigDecimal("air_yards_share"));
        stats.setWopr(base.getBigDecimal("wopr"));
        stats.setEpaPerTarget(IngestionMath.divide(base.getBigDecimal("receiving_epa"), targets, 4));

        if (advRow != null) {
            stats.setAdot(advRow.getBigDecimal("adot"));
            stats.setDropRate(advRow.getBigDecimal("drop_percent"));
            stats.setDrops(advRow.getInt("drop"));
            stats.setYacPerReception(advRow.getBigDecimal("yac_r"));
        }

        return stats;
    }
}
