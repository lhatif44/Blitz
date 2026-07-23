package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.PuntingStats;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class PuntingStatsMapper {

    public PuntingStats map(CsvRow base, Player player, Team team, Integer season, String seasonType) {
        PuntingStats stats = new PuntingStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        Integer punts = base.getInt("pt_att");
        Integer puntYards = base.getInt("pt_yards");
        stats.setPunts(punts);
        stats.setPuntYards(puntYards);
        stats.setGrossAvg(IngestionMath.divide(puntYards, punts, 2));
        stats.setNetAvg(IngestionMath.divide(base.getInt("pt_net_yards"), punts, 2));
        stats.setPuntsInside20(base.getInt("pt_inside_20"));
        stats.setTouchbacks(base.getInt("pt_touchback"));
        stats.setLongPunt(base.getInt("pt_long"));
        stats.setFairCatchesForced(base.getInt("pt_fair_caught"));

        return stats;
    }
}
