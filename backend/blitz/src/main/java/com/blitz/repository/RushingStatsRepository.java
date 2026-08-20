package com.blitz.repository;

import com.blitz.model.entity.RushingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RushingStatsRepository extends JpaRepository<RushingStats, UUID> {

    //Function to get all rushing stat lines for a player across all seasons
    List<RushingStats> findByPlayerId(UUID playerId);

    //Function to get all rushing stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<RushingStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's rushing stats for a specific season and season type e.g. REG or POST
    List<RushingStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);

    //Function to get every rushing stat row for a season/season type in one query — used by ingestion to
    //prefetch existing rows instead of querying once per CSV row
    List<RushingStats> findBySeasonAndSeasonType(Integer season, String seasonType);
}
