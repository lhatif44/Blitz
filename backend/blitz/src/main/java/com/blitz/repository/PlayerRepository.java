package com.blitz.repository;

import com.blitz.model.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    // find a player by their nflverse ID
    Optional<Player> findByNflverseId(String nflverseId);

    // search players by name — case insensitive, partial match
    List<Player> findByDisplayNameContainingIgnoreCase(String name);

    // get all players at a specific position group e.g. "QB", "CB"
    List<Player> findByPositionGroup(String positionGroup);

    // get all active or retired players
    List<Player> findByStatus(String status);
}
