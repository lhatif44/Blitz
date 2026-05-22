package com.blitz.service;

import com.blitz.model.entity.CareerPercentile;

import java.util.List;
import java.util.UUID;

public interface PercentileService {

    //Function to get all percentile rankings for a player
    List<CareerPercentile> getPercentilesForPlayer(UUID playerId);

    //Function to compute and store percentile rankings for a single player
    //Called after ingestion updates a player's stats
    void computePercentilesForPlayer(UUID playerId);

    //Function to compute and store percentile rankings for all players at a position group
    //Called by the weekly scheduled ingestion job
    void computePercentilesForPositionGroup(String positionGroup);

    //Function to compute and store percentile rankings for all players across all position groups
    //Full refresh — runs on the weekly ingestion schedule
    void computeAllPercentiles();
}
