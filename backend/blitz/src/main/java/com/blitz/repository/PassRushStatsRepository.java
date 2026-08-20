package com.blitz.repository;

import com.blitz.model.entity.PassRushStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PassRushStatsRepository extends JpaRepository<PassRushStats, UUID> {

    //Function to get all pass rush stat lines for a player across all seasons
    List<PassRushStats> findByPlayerId(UUID playerId);

    //Function to get all pass rush stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<PassRushStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's pass rush stats for a specific season and season type e.g. REG or POST
    List<PassRushStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);

    //Function to get every pass rush stat row for a season/season type in one query — used by ingestion to
    //prefetch existing rows instead of querying once per CSV row
    List<PassRushStats> findBySeasonAndSeasonType(Integer season, String seasonType);
}
