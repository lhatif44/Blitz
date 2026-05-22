package com.blitz.repository;

import com.blitz.model.entity.PassingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PassingStatsRepository extends JpaRepository<PassingStats, UUID> {

    //Function to get all passing stat lines for a player across all seasons
    List<PassingStats> findByPlayerId(UUID playerId);

    //Function to get all passing stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<PassingStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's passing stats for a specific season and season type e.g. REG or POST
    List<PassingStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);
}
