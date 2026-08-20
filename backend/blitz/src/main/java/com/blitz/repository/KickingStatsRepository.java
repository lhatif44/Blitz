package com.blitz.repository;

import com.blitz.model.entity.KickingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KickingStatsRepository extends JpaRepository<KickingStats, UUID> {

    //Function to get all kicking stat lines for a player across all seasons
    List<KickingStats> findByPlayerId(UUID playerId);

    //Function to get all kicking stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<KickingStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's kicking stats for a specific season and season type e.g. REG or POST
    List<KickingStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);

    //Function to get every kicking stat row for a season/season type in one query — used by ingestion to
    //prefetch existing rows instead of querying once per CSV row
    List<KickingStats> findBySeasonAndSeasonType(Integer season, String seasonType);
}
