package com.blitz.service;

import com.blitz.model.entity.*;

import java.util.List;
import java.util.UUID;

public interface StatsService {

    //Function to get all passing stats for a player
    List<PassingStats> getPassingStats(UUID playerId, String seasonType);

    //Function to get all rushing stats for a player
    List<RushingStats> getRushingStats(UUID playerId, String seasonType);

    //Function to get all receiving stats for a player
    List<ReceivingStats> getReceivingStats(UUID playerId, String seasonType);

    //Function to get all pass rush stats for a player
    List<PassRushStats> getPassRushStats(UUID playerId, String seasonType);

    //Function to get all linebacker stats for a player
    List<LinebackerStats> getLinebackerStats(UUID playerId, String seasonType);

    //Function to get all secondary stats for a player
    List<SecondaryStats> getSecondaryStats(UUID playerId, String seasonType);

    //Function to get all kicking stats for a player
    List<KickingStats> getKickingStats(UUID playerId, String seasonType);

    //Function to get all punting stats for a player
    List<PuntingStats> getPuntingStats(UUID playerId, String seasonType);

    //Function to save passing stats — used by ingestion pipeline
    PassingStats savePassingStats(PassingStats stats);

    //Function to save rushing stats — used by ingestion pipeline
    RushingStats saveRushingStats(RushingStats stats);

    //Function to save receiving stats — used by ingestion pipeline
    ReceivingStats saveReceivingStats(ReceivingStats stats);

    //Function to save pass rush stats — used by ingestion pipeline
    PassRushStats savePassRushStats(PassRushStats stats);

    //Function to save linebacker stats — used by ingestion pipeline
    LinebackerStats saveLinebackerStats(LinebackerStats stats);

    //Function to save secondary stats — used by ingestion pipeline
    SecondaryStats saveSecondaryStats(SecondaryStats stats);

    //Function to save kicking stats — used by ingestion pipeline
    KickingStats saveKickingStats(KickingStats stats);

    //Function to save punting stats — used by ingestion pipeline
    PuntingStats savePuntingStats(PuntingStats stats);
}
