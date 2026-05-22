package com.blitz.service;

import com.blitz.model.entity.Player;
import com.blitz.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
//All methods are read only unless overridden
@Transactional(readOnly = true)
public class CompareServiceImpl implements CompareService {

    //Repository used to look up player records by ID
    private final PlayerRepository playerRepository;

    //StatsService is used to fetch the correct stats table for each position group
    private final StatsService statsService;

    //Constructor injection for the player repository and stats service
    public CompareServiceImpl(PlayerRepository playerRepository, StatsService statsService) {
        this.playerRepository = playerRepository;
        this.statsService = statsService;
    }

    @Override
    //Function to validate that two players (looked up by UUID) are in the same position group
    //Fetches both players from the database, then delegates to the private helper for the actual check
    //Throws a RuntimeException if either player is not found or if their position groups don't match
    public void validateSamePositionGroup(UUID playerId1, UUID playerId2) {
        Player player1 = playerRepository.findById(playerId1)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId1));
        Player player2 = playerRepository.findById(playerId2)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId2));
        //Delegate to the overloaded helper that accepts Player objects directly
        validateSamePositionGroup(player1, player2);
    }

    //Private helper that performs the actual position group equality check
    //Accepts already-fetched Player objects to avoid making redundant database calls
    //Called internally by comparePlayers so players are only fetched once
    private void validateSamePositionGroup(Player player1, Player player2) {
        if (!player1.getPositionGroup().equals(player2.getPositionGroup())) {
            throw new RuntimeException("Cannot compare players from different position groups: "
                    + player1.getPositionGroup() + " vs " + player2.getPositionGroup());
        }
    }

    @Override
    //Function to compare two players head to head for a given season type (e.g. "REG" or "POST")
    //Fetches both players and validates they share a position group, then retrieves the correct
    //stats table for that position — returns both players and their stats in a single result object
    public CompareService.ComparisonResult comparePlayers(UUID playerId1, UUID playerId2, String seasonType) {
        //Fetch both players once — reused for the position group check and the result object
        Player player1 = playerRepository.findById(playerId1)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId1));
        Player player2 = playerRepository.findById(playerId2)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId2));

        //Throws if the two players are not in the same position group
        validateSamePositionGroup(player1, player2);

        String positionGroup = player1.getPositionGroup();

        //Each position group maps to a different stats table
        //WR and TE share receiving stats; DE, DT, EDGE share pass rush stats; CB and S share secondary stats
        Object player1Stats;
        Object player2Stats;

        switch (positionGroup) {
            case "QB" -> {
                player1Stats = statsService.getPassingStats(playerId1, seasonType);
                player2Stats = statsService.getPassingStats(playerId2, seasonType);
            }
            case "RB" -> {
                player1Stats = statsService.getRushingStats(playerId1, seasonType);
                player2Stats = statsService.getRushingStats(playerId2, seasonType);
            }
            case "WR", "TE" -> {
                player1Stats = statsService.getReceivingStats(playerId1, seasonType);
                player2Stats = statsService.getReceivingStats(playerId2, seasonType);
            }
            case "DE", "DT", "EDGE" -> {
                player1Stats = statsService.getPassRushStats(playerId1, seasonType);
                player2Stats = statsService.getPassRushStats(playerId2, seasonType);
            }
            case "LB" -> {
                player1Stats = statsService.getLinebackerStats(playerId1, seasonType);
                player2Stats = statsService.getLinebackerStats(playerId2, seasonType);
            }
            case "CB", "S" -> {
                player1Stats = statsService.getSecondaryStats(playerId1, seasonType);
                player2Stats = statsService.getSecondaryStats(playerId2, seasonType);
            }
            case "K" -> {
                player1Stats = statsService.getKickingStats(playerId1, seasonType);
                player2Stats = statsService.getKickingStats(playerId2, seasonType);
            }
            case "P" -> {
                player1Stats = statsService.getPuntingStats(playerId1, seasonType);
                player2Stats = statsService.getPuntingStats(playerId2, seasonType);
            }
            //Any position group not listed above is not supported for comparison
            default -> throw new RuntimeException("Unsupported position group: " + positionGroup);
        }

        return new CompareService.ComparisonResult(player1, player2, player1Stats, player2Stats, positionGroup);
    }
}
