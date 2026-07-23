package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.LinebackerStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class LinebackerStatsMapper {

    public LinebackerStats map(CsvRow base, CsvRow advRow, Player player, Team team, Integer season, String seasonType) {
        LinebackerStats stats = new LinebackerStats();
        stats.setPlayer(player);
        stats.setTeam(team);
        stats.setSeason(season);
        stats.setSeasonType(seasonType);
        stats.setGames(base.getInt("games"));

        Integer solo = base.getInt("def_tackles_solo");
        Integer assist = base.getInt("def_tackles_with_assist");
        stats.setTacklesSolo(solo);
        stats.setTacklesAssist(assist);
        stats.setTacklesTotal(IngestionMath.sum(solo, assist));

        stats.setSacks(base.getBigDecimal("def_sacks"));
        stats.setTfl(base.getBigDecimal("def_tackles_for_loss"));
        stats.setQbHits(base.getInt("def_qb_hits"));
        stats.setInterceptions(base.getInt("def_interceptions"));
        stats.setIntYards(base.getInt("def_interception_yards"));
        stats.setPassesDefended(base.getInt("def_pass_defended"));
        stats.setForcedFumbles(base.getInt("def_fumbles_forced"));
        stats.setFumbleRecoveries(IngestionMath.sum(base.getInt("fumble_recovery_own"), base.getInt("fumble_recovery_opp")));

        if (advRow != null) {
            stats.setTargetsAllowed(advRow.getInt("tgt"));
            stats.setReceptionsAllowed(advRow.getInt("cmp"));
            stats.setYardsAllowed(advRow.getInt("yds"));
            stats.setTdsAllowed(advRow.getInt("td"));
            stats.setPasserRatingAllowed(advRow.getBigDecimal("rat"));
            stats.setCompletionPctAllowed(advRow.getBigDecimal("cmp_percent"));
        }

        return stats;
    }
}
