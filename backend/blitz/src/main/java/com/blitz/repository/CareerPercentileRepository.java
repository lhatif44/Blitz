package com.blitz.repository;

import com.blitz.model.entity.CareerPercentile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CareerPercentileRepository extends JpaRepository<CareerPercentile, UUID> {

    //Function to get all percentile rankings for a specific player
    List<CareerPercentile> findByPlayerId(UUID playerId);

    //Function to get all percentile rankings for a position group e.g. all QB percentiles
    List<CareerPercentile> findByPositionGroup(String positionGroup);

    //Function to delete all existing percentiles for a player before recomputing
    void deleteByPlayerId(UUID playerId);

    //Function to delete all existing percentiles for a position group before recomputing
    void deleteByPositionGroup(String positionGroup);
}
