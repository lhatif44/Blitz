package com.blitz.service;

import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.blitz.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CareerStatsServiceImpl implements CareerStatsService {

    //Repositories for raw per-season stats — one per position group's stat type
    private final CareerStatsRepository careerStatsRepository;
    private final PlayerRepository playerRepository;
    private final PassingStatsRepository passingStatsRepository;
    private final RushingStatsRepository rushingStatsRepository;
    private final ReceivingStatsRepository receivingStatsRepository;
    private final PassRushStatsRepository passRushStatsRepository;
    private final LinebackerStatsRepository linebackerStatsRepository;
    private final SecondaryStatsRepository secondaryStatsRepository;
    private final KickingStatsRepository kickingStatsRepository;
    private final PuntingStatsRepository puntingStatsRepository;

    //Constructor injection for all repositories
    public CareerStatsServiceImpl(
            CareerStatsRepository careerStatsRepository,
            PlayerRepository playerRepository,
            PassingStatsRepository passingStatsRepository,
            RushingStatsRepository rushingStatsRepository,
            ReceivingStatsRepository receivingStatsRepository,
            PassRushStatsRepository passRushStatsRepository,
            LinebackerStatsRepository linebackerStatsRepository,
            SecondaryStatsRepository secondaryStatsRepository,
            KickingStatsRepository kickingStatsRepository,
            PuntingStatsRepository puntingStatsRepository) {
        this.careerStatsRepository = careerStatsRepository;
        this.playerRepository = playerRepository;
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
    //Function to return all stored career stat rows for a player
    //Used by the player profile page to display career totals next to season-by-season stats
    public List<CareerStats> getCareerStatsForPlayer(UUID playerId) {
        return careerStatsRepository.findByPlayerId(playerId);
    }

    @Override
    //Function to recompute career stats for a single player
    //Recomputes the whole position group because minimum thresholds depend on all players' data
    public void computeCareerStatsForPlayer(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));
        computeCareerStatsForPositionGroup(player.getPositionGroup());
    }

    @Override
    //Function to recompute career stats for all players at a given position group
    //Deletes existing rows then recomputes from the raw per-season stats tables
    public void computeCareerStatsForPositionGroup(String positionGroup) {
        //Delete existing career stats for this group before recomputing
        careerStatsRepository.deleteByPositionGroup(positionGroup);

        List<Player> players = playerRepository.findByPositionGroup(positionGroup);

        //Route to the correct aggregation method based on position group
        switch (positionGroup) {
            case "QB"               -> computeQBCareerStats(players);
            case "RB"               -> computeRBCareerStats(players);
            case "WR", "TE"         -> computeWRTECareerStats(players, positionGroup);
            case "DE", "DT", "EDGE" -> computePassRushCareerStats(players, positionGroup);
            case "LB"               -> computeLBCareerStats(players);
            case "CB", "S"          -> computeSecondaryCareerStats(players, positionGroup);
            case "K"                -> computeKickerCareerStats(players);
            case "P"                -> computePunterCareerStats(players);
            default -> throw new RuntimeException("Unsupported position group: " + positionGroup);
        }
    }

    @Override
    //Function to recompute career stats for every position group
    //Called by the weekly ingestion job for a full refresh
    public void computeAllCareerStats() {
        List.of("QB", "RB", "WR", "TE", "DE", "DT", "EDGE", "LB", "CB", "S", "K", "P")
                .forEach(this::computeCareerStatsForPositionGroup);
    }

    //Aggregates career totals for QBs — minimum 500 regular season pass attempts to qualify
    //Rate stats (completion %, yards per attempt, EPA) use weighted averages to account for
    //players with very different volumes of attempts across seasons
    private void computeQBCareerStats(List<Player> players) {
        for (Player player : players) {
            //Only count regular season rows — playoff stats don't count toward career thresholds
            List<PassingStats> stats = passingStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalAttempts = sumInt(stats, s -> s.getAttempts());
            //Skip players who haven't thrown enough to qualify for career rankings
            if (totalAttempts < 500) continue;

            int totalYards       = sumInt(stats, s -> s.getPassingYards());
            int totalTDs         = sumInt(stats, s -> s.getPassingTds());
            int totalCompletions = sumInt(stats, s -> s.getCompletions());

            //Rate stats computed from career totals rather than averaged season-to-season
            //to avoid weighting a low-attempt season the same as a high-attempt season
            double compPct = totalAttempts > 0 ? (double) totalCompletions / totalAttempts * 100 : 0;
            double ypa     = totalAttempts > 0 ? (double) totalYards / totalAttempts : 0;
            double epa     = weightedAvg(stats, s -> s.getEpaPerDropback(), s -> s.getAttempts());

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_passing_yards",     bd(totalYards));
            totals.put("career_passing_tds",       bd(totalTDs));
            totals.put("career_completion_pct",    bd(compPct, 2));
            totals.put("career_yards_per_attempt", bd(ypa, 2));
            totals.put("career_epa_per_dropback",  bd(epa, 3));

            saveCareerStats(player, totals, "QB");
        }
    }

    //Aggregates career totals for RBs — minimum 200 regular season carries to qualify
    private void computeRBCareerStats(List<Player> players) {
        for (Player player : players) {
            List<RushingStats> stats = rushingStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalCarries = sumInt(stats, s -> s.getCarries());
            if (totalCarries < 200) continue;

            int totalRushYards = sumInt(stats, s -> s.getRushingYards());
            int totalRushTDs   = sumInt(stats, s -> s.getRushingTds());
            int totalRecYards  = sumInt(stats, s -> s.getReceivingYards());
            int brokenTackles  = sumInt(stats, s -> s.getBrokenTackles());

            //Yards from scrimmage combines rushing yards and receiving yards
            int yardsFromScrimmage = totalRushYards + totalRecYards;
            double ypc = totalCarries > 0 ? (double) totalRushYards / totalCarries : 0;

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_rushing_yards",        bd(totalRushYards));
            totals.put("career_rushing_tds",          bd(totalRushTDs));
            totals.put("career_yards_per_carry",      bd(ypc, 2));
            totals.put("career_yards_from_scrimmage", bd(yardsFromScrimmage));
            totals.put("career_broken_tackles",       bd(brokenTackles));

            saveCareerStats(player, totals, "RB");
        }
    }

    //Aggregates career totals for WRs and TEs — minimum 100 regular season targets to qualify
    //WR and TE share the receiving_stats table, so one method handles both
    private void computeWRTECareerStats(List<Player> players, String positionGroup) {
        for (Player player : players) {
            List<ReceivingStats> stats = receivingStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalTargets = sumInt(stats, s -> s.getTargets());
            if (totalTargets < 100) continue;

            int totalReceptions = sumInt(stats, s -> s.getReceptions());
            int totalYards      = sumInt(stats, s -> s.getReceivingYards());
            int totalTDs        = sumInt(stats, s -> s.getReceivingTds());

            //Catch rate and yards per target are rate stats — compute from career totals
            double catchRate = totalTargets > 0 ? (double) totalReceptions / totalTargets * 100 : 0;
            double ypt       = totalTargets > 0 ? (double) totalYards / totalTargets : 0;

            //WOPR (weighted opportunity rating) combines target and air yards share — weighted by targets
            double wopr = weightedAvg(stats, s -> s.getWopr(), s -> s.getTargets());

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_receiving_yards",  bd(totalYards));
            totals.put("career_receiving_tds",    bd(totalTDs));
            totals.put("career_catch_rate",       bd(catchRate, 2));
            totals.put("career_yards_per_target", bd(ypt, 2));
            totals.put("career_wopr",             bd(wopr, 3));

            saveCareerStats(player, totals, positionGroup);
        }
    }

    //Aggregates career totals for pass rushers (DE, DT, EDGE) — minimum 32 games to qualify
    private void computePassRushCareerStats(List<Player> players, String positionGroup) {
        for (Player player : players) {
            List<PassRushStats> stats = passRushStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalGames = sumInt(stats, s -> s.getGames());
            if (totalGames < 32) continue;

            double totalSacks  = sumDouble(stats, s -> s.getSacks());
            double totalTFL    = sumDouble(stats, s -> s.getTfl());
            int totalPressures = sumInt(stats, s -> s.getPressures());

            //Pass rush win rate is a per-snap rate stat — weighted by snaps played each season
            double prwr = weightedAvg(stats, s -> s.getPassRushWinRate(), s -> s.getSnaps());

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_sacks",              bd(totalSacks, 1));
            totals.put("career_tfl",                bd(totalTFL, 1));
            totals.put("career_pressures",          bd(totalPressures));
            totals.put("career_pass_rush_win_rate", bd(prwr, 2));

            saveCareerStats(player, totals, positionGroup);
        }
    }

    //Aggregates career totals for linebackers — minimum 32 games to qualify
    private void computeLBCareerStats(List<Player> players) {
        for (Player player : players) {
            List<LinebackerStats> stats = linebackerStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalGames = sumInt(stats, s -> s.getGames());
            if (totalGames < 32) continue;

            int totalTackles  = sumInt(stats, s -> s.getTacklesTotal());
            double totalSacks = sumDouble(stats, s -> s.getSacks());
            int totalINTs     = sumInt(stats, s -> s.getInterceptions());
            int totalPBUs     = sumInt(stats, s -> s.getPassesDefended());

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_tackles",         bd(totalTackles));
            totals.put("career_sacks",           bd(totalSacks, 1));
            totals.put("career_interceptions",   bd(totalINTs));
            totals.put("career_passes_defended", bd(totalPBUs));

            saveCareerStats(player, totals, "LB");
        }
    }

    //Aggregates career totals for cornerbacks and safeties — minimum 32 games to qualify
    //CB and S share the secondary_stats table, so one method handles both
    private void computeSecondaryCareerStats(List<Player> players, String positionGroup) {
        for (Player player : players) {
            List<SecondaryStats> stats = secondaryStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            int totalGames = sumInt(stats, s -> s.getGames());
            if (totalGames < 32) continue;

            int totalINTs     = sumInt(stats, s -> s.getInterceptions());
            int totalPBUs     = sumInt(stats, s -> s.getPassesDefended());

            //Passer rating allowed is a rate stat — weighted by targets to avoid inflation
            //from low-sample seasons; a lower value is better (stored as-is)
            double praAllowed = weightedAvg(stats, s -> s.getPasserRatingAllowed(), s -> s.getTargetsAllowed());

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_interceptions",         bd(totalINTs));
            totals.put("career_passes_defended",       bd(totalPBUs));
            totals.put("career_passer_rating_allowed", bd(praAllowed, 2));

            saveCareerStats(player, totals, positionGroup);
        }
    }

    //Aggregates career totals for kickers — minimum 2 seasons to qualify
    private void computeKickerCareerStats(List<Player> players) {
        for (Player player : players) {
            List<KickingStats> stats = kickingStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            if (stats.size() < 2) continue;

            int totalFGMade = sumInt(stats, s -> s.getFgMade());
            int totalFGAtt  = sumInt(stats, s -> s.getFgAttempted());
            int total50Made = sumInt(stats, s -> s.getFg50PlusMade());
            int total50Att  = sumInt(stats, s -> s.getFg50PlusAttempted());
            int totalXPMade = sumInt(stats, s -> s.getXpMade());
            int totalXPAtt  = sumInt(stats, s -> s.getXpAttempted());

            //All three are percentages computed from career attempt totals
            double fgPct   = totalFGAtt > 0 ? (double) totalFGMade / totalFGAtt * 100 : 0;
            double fg50Pct = total50Att > 0 ? (double) total50Made / total50Att * 100 : 0;
            double xpPct   = totalXPAtt > 0 ? (double) totalXPMade / totalXPAtt * 100 : 0;

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_fg_pct",    bd(fgPct, 2));
            totals.put("career_fg_50_pct", bd(fg50Pct, 2));
            totals.put("career_xp_pct",    bd(xpPct, 2));

            saveCareerStats(player, totals, "K");
        }
    }

    //Aggregates career totals for punters — minimum 2 seasons to qualify
    private void computePunterCareerStats(List<Player> players) {
        for (Player player : players) {
            List<PuntingStats> stats = puntingStatsRepository.findByPlayerId(player.getId())
                    .stream().filter(s -> "REG".equals(s.getSeasonType())).toList();

            if (stats.size() < 2) continue;

            int totalPunts    = sumInt(stats, s -> s.getPunts());
            int totalInside20 = sumInt(stats, s -> s.getPuntsInside20());

            //Net average weighted by punts per season — punters with more punts carry more weight
            double netAvg      = weightedAvg(stats, s -> s.getNetAvg(), s -> s.getPunts());
            double inside20Pct = totalPunts > 0 ? (double) totalInside20 / totalPunts * 100 : 0;

            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            totals.put("career_net_avg",       bd(netAvg, 2));
            totals.put("career_inside_20_pct", bd(inside20Pct, 2));

            saveCareerStats(player, totals, "P");
        }
    }

    //Saves the computed career stat map for a single player to the career_stats table
    //Each key in the map becomes its own row e.g. "career_passing_yards" → 52000
    private void saveCareerStats(Player player, Map<String, BigDecimal> totals, String positionGroup) {
        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            CareerStats cs = new CareerStats();
            cs.setPlayer(player);
            cs.setPositionGroup(positionGroup);
            cs.setStatName(entry.getKey());
            cs.setStatValue(entry.getValue());
            cs.setComputedAt(now);
            careerStatsRepository.save(cs);
        }
    }

    //Helper to sum an integer field across a list of stat rows, treating nulls as 0
    private <T> int sumInt(List<T> list, java.util.function.Function<T, Integer> getter) {
        return list.stream().mapToInt(s -> getter.apply(s) != null ? getter.apply(s) : 0).sum();
    }

    //Helper to sum a decimal field across a list of stat rows, treating nulls as 0
    private <T> double sumDouble(List<T> list, java.util.function.Function<T, BigDecimal> getter) {
        return list.stream().mapToDouble(s -> getter.apply(s) != null ? getter.apply(s).doubleValue() : 0).sum();
    }

    //Helper to compute a weighted average of a decimal field, weighted by an integer field
    //e.g. EPA per dropback weighted by attempts — seasons with more attempts count more
    private <T> double weightedAvg(List<T> list,
                                   java.util.function.Function<T, BigDecimal> valueGetter,
                                   java.util.function.Function<T, Integer> weightGetter) {
        double totalWeight = list.stream()
                .mapToDouble(s -> weightGetter.apply(s) != null ? weightGetter.apply(s) : 0).sum();
        if (totalWeight == 0) return 0;
        double weightedSum = list.stream()
                .filter(s -> valueGetter.apply(s) != null && weightGetter.apply(s) != null)
                .mapToDouble(s -> valueGetter.apply(s).doubleValue() * weightGetter.apply(s))
                .sum();
        return weightedSum / totalWeight;
    }

    //Helper to convert a number to BigDecimal with no decimal places
    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
    }

    //Helper to convert a number to BigDecimal with a specified number of decimal places
    private BigDecimal bd(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }
}
