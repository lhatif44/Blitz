package com.blitz.repository;

import com.blitz.model.entity.SecondaryStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecondaryStatsRepository extends JpaRepository<SecondaryStats, UUID> {

    //Function to get all secondary stat lines for a player across all seasons
    List<SecondaryStats> findByPlayerId(UUID playerId);

    //Function to get all secondary stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<SecondaryStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's secondary stats for a specific season and season type e.g. REG or POST
    List<SecondaryStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);
}
