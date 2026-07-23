package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.RushingStats;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class RushingStatsMapper {

    public RushingStats map(CsvRow base, CsvRow advRow, Player player, Team team, Integer season, String seasonType) {
        RushingStats stats = new RushingStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        Integer carries = base.getInt("carries");
        Integer rushingYards = base.getInt("rushing_yards");
        stats.setCarries(carries);
        stats.setRushingYards(rushingYards);
        stats.setRushingTds(base.getInt("rushing_tds"));
        stats.setFumbles(base.getInt("rushing_fumbles"));
        stats.setFumblesLost(base.getInt("rushing_fumbles_lost"));
        stats.setRushingFirstDowns(base.getInt("rushing_first_downs"));
        stats.setYardsPerCarry(IngestionMath.divide(rushingYards, carries, 2));
        stats.setRushingEpa(base.getBigDecimal("rushing_epa"));

        Integer targets = base.getInt("targets");
        Integer receptions = base.getInt("receptions");
        Integer receivingYards = base.getInt("receiving_yards");
        stats.setTargets(targets);
        stats.setReceptions(receptions);
        stats.setReceivingYards(receivingYards);
        stats.setReceivingTds(base.getInt("receiving_tds"));
        stats.setYardsPerReception(IngestionMath.divide(receivingYards, receptions, 2));

        if (advRow != null) {
            stats.setYardsAfterContact(advRow.getBigDecimal("ybc_att"));
            stats.setBrokenTackles(advRow.getInt("brk_tkl"));
            stats.setYacPerCarry(advRow.getBigDecimal("yac_att"));
        }

        return stats;
    }
}
