package com.blitz.repository;

import com.blitz.model.entity.CareerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CareerStatsRepository extends JpaRepository<CareerStats, UUID> {

    //Function to get all career stat rows for a specific player
    List<CareerStats> findByPlayerId(UUID playerId);

    //Function to get all career stat rows for a position group e.g. all QB career stats
    //Used by PercentileService to rank all players at a position against each other
    List<CareerStats> findByPositionGroup(String positionGroup);

    //Function to delete all career stats for a player before recomputing
    void deleteByPlayerId(UUID playerId);

    //Function to delete all career stats for a position group before recomputing
    void deleteByPositionGroup(String positionGroup);
}
