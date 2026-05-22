package com.blitz.service;

import com.blitz.model.entity.Player;

import java.util.UUID;

public interface CompareService {

    //Function to validate that two players are in the same position group
    //Throws exception if they are not — comparison only allowed within same position group
    void validateSamePositionGroup(UUID playerId1, UUID playerId2);

    //Function to get a full comparison between two players
    //Returns both players and their stats in a single response object
    //Throws exception if players are not in the same position group
    ComparisonResult comparePlayers(UUID playerId1, UUID playerId2, String seasonType);

    //Inner class that holds the result of a head to head comparison
    class ComparisonResult {

        private final Player player1;
        private final Player player2;
        private final Object player1Stats; // stats object varies by position group
        private final Object player2Stats;
        private final String positionGroup;

        public ComparisonResult(Player player1, Player player2, Object player1Stats, Object player2Stats, String positionGroup) {
            this.player1 = player1;
            this.player2 = player2;
            this.player1Stats = player1Stats;
            this.player2Stats = player2Stats;
            this.positionGroup = positionGroup;
        }

        public Player getPlayer1() { return player1; }
        public Player getPlayer2() { return player2; }
        public Object getPlayer1Stats() { return player1Stats; }
        public Object getPlayer2Stats() { return player2Stats; }
        public String getPositionGroup() { return positionGroup; }
    }
}
