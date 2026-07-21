package com.blitz.service;

import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
//All methods are read only unless overridden
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    //Repositories for each stats table — one per position group's stat type
    private final PassingStatsRepository passingStatsRepository;
    private final RushingStatsRepository rushingStatsRepository;
    private final ReceivingStatsRepository receivingStatsRepository;
    private final PassRushStatsRepository passRushStatsRepository;
    private final LinebackerStatsRepository linebackerStatsRepository;
    private final SecondaryStatsRepository secondaryStatsRepository;
    private final KickingStatsRepository kickingStatsRepository;
    private final PuntingStatsRepository puntingStatsRepository;

    //Constructor injection for all repositories
    public StatsServiceImpl(
            PassingStatsRepository passingStatsRepository,
            RushingStatsRepository rushingStatsRepository,
            ReceivingStatsRepository receivingStatsRepository,
            PassRushStatsRepository passRushStatsRepository,
            LinebackerStatsRepository linebackerStatsRepository,
            SecondaryStatsRepository secondaryStatsRepository,
            KickingStatsRepository kickingStatsRepository,
            PuntingStatsRepository puntingStatsRepository) {
        
	    
	this.passingStatsRepository = passingStatsRepository;
        
	this.rushingStatsRepository = rushingStatsRepository;
        
	this.receivingStatsRepository = receivingStatsRepository;
        
	this.passRushStatsRepository = passRushStatsRepository;
        
	this.linebackerStatsRepository = linebackerStatsRepository;
        
	this.secondaryStatsRepository = secondaryStatsRepository;
        
	this.kickingStatsRepository = kickingStatsRepository;
        
	this.puntingStatsRepository = puntingStatsRepository;
    }

    @Override
    //Function to get passing stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<PassingStats> getPassingStats(UUID playerId, String seasonType) {
        
	if (seasonType != null && !seasonType.isBlank()) {
            return passingStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return passingStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get rushing stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<RushingStats> getRushingStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return rushingStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return rushingStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get receiving stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<ReceivingStats> getReceivingStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return receivingStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return receivingStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get pass rush stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<PassRushStats> getPassRushStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return passRushStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return passRushStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get linebacker stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<LinebackerStats> getLinebackerStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return linebackerStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return linebackerStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get secondary stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<SecondaryStats> getSecondaryStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return secondaryStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return secondaryStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get kicking stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<KickingStats> getKickingStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return kickingStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return kickingStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to get punting stats for a player
    //If seasonType is provided (e.g. "REG" or "POST"), only rows matching that type are returned
    //If seasonType is null or blank, all rows across every season are returned
    public List<PuntingStats> getPuntingStats(UUID playerId, String seasonType) {
        if (seasonType != null && !seasonType.isBlank()) {
            return puntingStatsRepository.findByPlayerIdAndSeasonType(playerId, seasonType);
        }
        return puntingStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Overrides readOnly — writes a passing stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public PassingStats savePassingStats(PassingStats stats) {
        return passingStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a rushing stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public RushingStats saveRushingStats(RushingStats stats) {
        return rushingStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a receiving stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public ReceivingStats saveReceivingStats(ReceivingStats stats) {
        return receivingStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a pass rush stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public PassRushStats savePassRushStats(PassRushStats stats) {
        return passRushStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a linebacker stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public LinebackerStats saveLinebackerStats(LinebackerStats stats) {
        return linebackerStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a secondary stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public SecondaryStats saveSecondaryStats(SecondaryStats stats) {
        return secondaryStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a kicking stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public KickingStats saveKickingStats(KickingStats stats) {
        return kickingStatsRepository.save(stats);
    }

    @Override
    //Overrides readOnly — writes a punting stats row to the database
    //Used by the ingestion pipeline when loading new season data
    @Transactional
    public PuntingStats savePuntingStats(PuntingStats stats) {
        return puntingStatsRepository.save(stats);
    }
}
