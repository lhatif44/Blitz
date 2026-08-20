package com.blitz.repository;

import com.blitz.model.entity.ReceivingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceivingStatsRepository extends JpaRepository<ReceivingStats, UUID> {

    //Function to get all receiving stat lines for a player across all seasons
    List<ReceivingStats> findByPlayerId(UUID playerId);

    //Function to get all receiving stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<ReceivingStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's receiving stats for a specific season and season type e.g. REG or POST
    List<ReceivingStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);

    //Function to get every receiving stat row for a season/season type in one query — used by ingestion to
    //prefetch existing rows instead of querying once per CSV row
    List<ReceivingStats> findBySeasonAndSeasonType(Integer season, String seasonType);
}
