package com.blitz.repository;

import com.blitz.model.entity.PuntingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PuntingStatsRepository extends JpaRepository<PuntingStats, UUID> {

    //Function to get all punting stat lines for a player across all seasons
    List<PuntingStats> findByPlayerId(UUID playerId);

    //Function to get all punting stat rows for a player filtered by season type e.g. "REG" or "POST"
    List<PuntingStats> findByPlayerIdAndSeasonType(UUID playerId, String seasonType);

    //Function to get a player's punting stats for a specific season and season type e.g. REG or POST
    List<PuntingStats> findByPlayerIdAndSeasonAndSeasonType(UUID playerId, Integer season, String seasonType);
}
