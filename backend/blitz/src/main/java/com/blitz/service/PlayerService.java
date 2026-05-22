package com.blitz.service;

import com.blitz.model.entity.Player;
import java.util.List;
import java.util.UUID;

public interface PlayerService {
    
    // get a single player by their internal ID
    Player getPlayerById(UUID id);

    // get a single player by their nflverse ID
    Player getPlayerByNflverseId(String nflverseId);

    // search players by name — powers the search bar
    List<Player> searchPlayersByName(String name);

    // get all players at a position group e.g. "QB", "CB"
    List<Player> getPlayersByPositionGroup(String positionGroup);

    // get all active players
    List<Player> getActivePlayers();

    // get all players (active + retired)
    List<Player> getAllPlayers();

    // save or update a player — used by the ingestion pipeline
    Player savePlayer(Player player);
}
