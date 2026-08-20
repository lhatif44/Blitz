package com.blitz.repository;

import com.blitz.model.entity.LinebackerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface
LinebackerStatsRepository extends JpaRepository<LinebackerStats, UUID> {

    //Function to get all linebacker stat lines for a player across all seasons
    List<LinebackerStats> findByPlayerId(UUID playerId);

    //Function to get all linebacker stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<LinebackerStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's linebacker stats for a specific season and season type e.g. REG or POST
    List<LinebackerStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);

    //Function to get every linebacker stat row for a season/season type in one query — used by ingestion to
    //prefetch existing rows instead of querying once per CSV row
    List<LinebackerStats> findBySeasonAndSeasonType(Integer season, String seasonType);
}
