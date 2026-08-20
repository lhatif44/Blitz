package com.blitz.repository;

import com.blitz.model.entity.PlayerTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, UUID> {

    // Function to get every player-team row for one season in one query — used by ingestion to
    // prefetch which (player, team) pairs are already recorded instead of a query per CSV row
    List<PlayerTeam> findBySeasonStart(Integer seasonStart);
}
