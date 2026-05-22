package com.blitz.service;

import com.blitz.model.entity.CareerStats;

import java.util.List;
import java.util.UUID;

public interface CareerStatsService {

    //Function to get all stored career stats for a player
    //Used by the player profile page to display career totals
    List<CareerStats> getCareerStatsForPlayer(UUID playerId);

    //Function to recompute career stats for a single player
    //Called after the ingestion pipeline updates a player's season data
    void computeCareerStatsForPlayer(UUID playerId);

    //Function to recompute career stats for all players at a position group
    //Called by the weekly ingestion job after loading a full position group
    void computeCareerStatsForPositionGroup(String positionGroup);

    //Function to recompute career stats for all players across all position groups
    //Full refresh — runs on the weekly ingestion schedule
    void computeAllCareerStats();
}
