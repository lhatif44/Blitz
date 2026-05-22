package com.blitz.service;

import com.blitz.model.entity.CareerPercentile;
import com.blitz.model.entity.CareerStats;
import com.blitz.repository.CareerPercentileRepository;
import com.blitz.repository.CareerStatsRepository;
import com.blitz.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
//Reads pre-computed career stats and ranks each player within their position group
//Does not touch raw season stats — CareerStatsService handles that aggregation
public class PercentileServiceImpl implements PercentileService {

    //CareerStatsRepository provides the pre-aggregated totals to rank
    private final CareerStatsRepository careerStatsRepository;
    private final CareerPercentileRepository careerPercentileRepository;
    private final PlayerRepository playerRepository;

    //Constructor injection for all repositories
    public PercentileServiceImpl(
            CareerStatsRepository careerStatsRepository,
            CareerPercentileRepository careerPercentileRepository,
            PlayerRepository playerRepository) {
        this.careerStatsRepository = careerStatsRepository;
        this.careerPercentileRepository = careerPercentileRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    //Function to get all stored percentile rankings for a player
    public List<CareerPercentile> getPercentilesForPlayer(UUID playerId) {
        return careerPercentileRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to recompute percentiles for a single player
    //Recomputes the whole position group since percentile ranks are relative to all players
    public void computePercentilesForPlayer(UUID playerId) {
        playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));
        //Percentiles are relative — changing one player's career stats shifts everyone's rank
        //so we must recompute the entire position group, not just the individual player
        computePercentilesForPositionGroup(
                playerRepository.findById(playerId).get().getPositionGroup());
    }

    @Override
    //Function to compute percentile rankings for all qualifying players at a position group
    //Reads career_stats rows (pre-computed by CareerStatsService), groups them by stat name,
    //then ranks each player's value within the group to produce a 0-100 percentile score
    public void computePercentilesForPositionGroup(String positionGroup) {
        //Remove stale percentile rows before writing the new rankings
        careerPercentileRepository.deleteByPositionGroup(positionGroup);

        //Pull all pre-computed career stats for this position group
        List<CareerStats> allCareerStats = careerStatsRepository.findByPositionGroup(positionGroup);
        if (allCareerStats.isEmpty()) return;

        //Group rows by stat name so we can rank all players for one stat at a time
        //e.g. { "career_passing_yards" → [Mahomes:52000, Allen:45000, ...], ... }
        Map<String, List<CareerStats>> byStatName = allCareerStats.stream()
                .collect(Collectors.groupingBy(CareerStats::getStatName));

        for (Map.Entry<String, List<CareerStats>> entry : byStatName.entrySet()) {
            String statName = entry.getKey();

            //Sort all values ascending so we can count how many players fall below each value
            List<BigDecimal> sortedValues = entry.getValue().stream()
                    .map(CareerStats::getStatValue)
                    .sorted()
                    .toList();

            //Compute and save a percentile record for each player for this stat
            for (CareerStats cs : entry.getValue()) {
                BigDecimal percentile = computePercentile(sortedValues, cs.getStatValue());

                CareerPercentile cp = new CareerPercentile();
                cp.setPlayer(cs.getPlayer());
                cp.setPositionGroup(positionGroup);
                cp.setStatName(statName);
                cp.setStatValue(cs.getStatValue());
                cp.setPercentile(percentile);
                cp.setComputedAt(LocalDateTime.now());

                careerPercentileRepository.save(cp);
            }
        }
    }

    @Override
    //Function to recompute percentiles for all players across every position group
    //Called by the weekly ingestion job after CareerStatsService has finished its full refresh
    public void computeAllPercentiles() {
        List.of("QB", "RB", "WR", "TE", "DE", "DT", "EDGE", "LB", "CB", "S", "K", "P")
                .forEach(this::computePercentilesForPositionGroup);
    }

    //Computes what percentile a player's value falls at relative to all qualifying players
    //Uses PERCENT_RANK logic: rank = number of players with a strictly lower value
    //e.g. if 10 players qualify and this player ranks 8th, percentile = 8/9 * 100 ≈ 88.9
    private BigDecimal computePercentile(List<BigDecimal> sortedValues, BigDecimal playerValue) {
        //If only one player qualifies, they are by definition at the 100th percentile
        if (sortedValues.size() <= 1) return BigDecimal.valueOf(100);
        long rank = sortedValues.stream().filter(v -> v.compareTo(playerValue) < 0).count();
        double percentile = (double) rank / (sortedValues.size() - 1) * 100;
        return BigDecimal.valueOf(percentile).setScale(1, RoundingMode.HALF_UP);
    }
}
