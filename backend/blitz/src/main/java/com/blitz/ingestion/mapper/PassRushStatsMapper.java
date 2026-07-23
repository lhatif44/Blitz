package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.PassRushStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class PassRushStatsMapper {

    public PassRushStats map(CsvRow base, CsvRow advRow, Player player, Team team, Integer season, String seasonType) {
        PassRushStats stats = new PassRushStats();
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
        stats.setForcedFumbles(base.getInt("def_fumbles_forced"));
        stats.setFumbleRecoveries(IngestionMath.sum(base.getInt("fumble_recovery_own"), base.getInt("fumble_recovery_opp")));
        stats.setFumbleReturnTds(base.getInt("fumble_recovery_tds"));

        if (advRow != null) {
            stats.setQbHurries(advRow.getInt("hrry"));
            stats.setPressures(advRow.getInt("prss"));
        }

        return stats;
    }
}
