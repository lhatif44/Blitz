package com.blitz.service;

import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareerStatsServiceTest {

    @Mock private CareerStatsRepository careerStatsRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private PassingStatsRepository passingStatsRepository;
    @Mock private RushingStatsRepository rushingStatsRepository;
    @Mock private ReceivingStatsRepository receivingStatsRepository;
    @Mock private PassRushStatsRepository passRushStatsRepository;
    @Mock private LinebackerStatsRepository linebackerStatsRepository;
    @Mock private SecondaryStatsRepository secondaryStatsRepository;
    @Mock private KickingStatsRepository kickingStatsRepository;
    @Mock private PuntingStatsRepository puntingStatsRepository;

    @InjectMocks
    private CareerStatsServiceImpl careerStatsService;

    private UUID playerId;
    private Player player;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        player = makePlayer(playerId, "QB");
    }

    // -------------------------------------------------------------------------
    // Basic behaviour
    // -------------------------------------------------------------------------

    @Test
    //Function that returns stored career stats from the repository
    void getCareerStatsForPlayer_returnsResultsFromRepository() {
        CareerStats cs = new CareerStats();
        cs.setStatName("career_passing_yards");
        cs.setStatValue(BigDecimal.valueOf(52000));

        when(careerStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(cs));

        List<CareerStats> result = careerStatsService.getCareerStatsForPlayer(playerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatName()).isEqualTo("career_passing_yards");
        verify(careerStatsRepository).findByPlayerId(playerId);
    }

    @Test
    //Function that throws when the player ID does not exist in the database
    void computeCareerStatsForPlayer_throwsException_whenPlayerNotFound() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careerStatsService.computeCareerStatsForPlayer(playerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    //Function that deletes existing career stats before recomputing
    void computeCareerStatsForPositionGroup_deletesExistingBeforeRecomputing() {
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of());

        careerStatsService.computeCareerStatsForPositionGroup("QB");

        verify(careerStatsRepository).deleteByPositionGroup("QB");
    }

    @Test
    //Function that throws for a position group not handled by the switch statement
    void computeCareerStatsForPositionGroup_throwsForUnsupportedGroup() {
        assertThatThrownBy(() -> careerStatsService.computeCareerStatsForPositionGroup("OL"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unsupported position group");
    }

    @Test
    //Function that triggers a delete-and-recompute for all 12 supported position groups
    void computeAllCareerStats_computesEveryPositionGroup() {
        List<String> allGroups = List.of("QB", "RB", "WR", "TE", "DE", "DT", "EDGE", "LB", "CB", "S", "K", "P");
        for (String group : allGroups) {
            when(playerRepository.findByPositionGroup(group)).thenReturn(List.of());
        }

        careerStatsService.computeAllCareerStats();

        for (String group : allGroups) {
            verify(careerStatsRepository).deleteByPositionGroup(group);
        }
    }

    // -------------------------------------------------------------------------
    // QB calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips QBs with fewer than 500 career regular season pass attempts
    void QB_excludesPlayersBelowAttemptThreshold() {
        PassingStats lowVolume = passingRow("REG", 100, 800, 5, 65, 0.10);

        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("QB");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that saves exactly 5 career stat rows for a qualifying QB
    //Stats: career_passing_yards, career_passing_tds, career_completion_pct, career_yards_per_attempt, career_epa_per_dropback
    void QB_savesCorrectNumberOfStats() {
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId))
                .thenReturn(List.of(passingRow("REG", 600, 5000, 40, 400, 0.25)));

        careerStatsService.computeCareerStatsForPositionGroup("QB");

        verify(careerStatsRepository, times(5)).save(any(CareerStats.class));
    }

    @Test
    //Function that correctly sums passing yards and TDs across multiple seasons
    void QB_aggregatesYardsAndTDsAcrossSeasons() {
        //Season 1: 2500 yards, 20 TDs — Season 2: 3000 yards, 25 TDs — Total: 5500 yards, 45 TDs
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(
                passingRow("REG", 300, 2500, 20, 200, 0.20),
                passingRow("REG", 300, 3000, 25, 210, 0.30)
        ));

        List<CareerStats> saved = captureStats("QB");

        assertStatValue(saved, "career_passing_yards", 5500);
        assertStatValue(saved, "career_passing_tds",   45);
    }

    @Test
    //Function that computes completion percentage from career totals, not averaged per season
    //Expected: (410 completions / 600 attempts) * 100 = 68.33%
    void QB_computesCompletionPctFromCareerTotals() {
        //410 completions out of 600 attempts across two seasons
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(
                passingRow("REG", 300, 2500, 20, 200, 0.20),
                passingRow("REG", 300, 3000, 25, 210, 0.30)
        ));
        //200 + 210 = 410 completions / 600 attempts = 68.33%

        List<CareerStats> saved = captureStats("QB");

        assertStatValue(saved, "career_completion_pct", new BigDecimal("68.33"));
    }

    @Test
    //Function that computes yards per attempt from career totals, not averaged per season
    //Expected: 5500 yards / 600 attempts = 9.17
    void QB_computesYardsPerAttemptFromCareerTotals() {
        //5500 total yards / 600 total attempts = 9.1666 → 9.17
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(
                passingRow("REG", 300, 2500, 20, 200, 0.20),
                passingRow("REG", 300, 3000, 25, 210, 0.30)
        ));

        List<CareerStats> saved = captureStats("QB");

        assertStatValue(saved, "career_yards_per_attempt", new BigDecimal("9.17"));
    }

    @Test
    //Function that computes EPA per dropback as a weighted average by attempts
    //Seasons with more attempts carry more weight in the average
    //Season 1: EPA 0.10, 200 attempts — Season 2: EPA 0.30, 400 attempts
    //Expected: (0.10*200 + 0.30*400) / 600 = 140/600 = 0.233
    void QB_computesEPAAsWeightedAvgByAttempts() {
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(
                passingRow("REG", 200, 1800, 14, 130, 0.10),
                passingRow("REG", 400, 3800, 30, 280, 0.30)
        ));

        List<CareerStats> saved = captureStats("QB");

        assertStatValue(saved, "career_epa_per_dropback", new BigDecimal("0.233"));
    }

    @Test
    //Function that ignores playoff (POST) rows when computing QB career stats
    void QB_ignoresPlayoffStats() {
        //Only has POST rows — should not qualify for career stats
        when(playerRepository.findByPositionGroup("QB")).thenReturn(List.of(player));
        when(passingStatsRepository.findByPlayerId(playerId))
                .thenReturn(List.of(passingRow("POST", 600, 5000, 40, 400, 0.25)));

        careerStatsService.computeCareerStatsForPositionGroup("QB");

        verify(careerStatsRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // RB calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips RBs with fewer than 200 career regular season carries
    void RB_excludesPlayersBelowCarryThreshold() {
        Player rb = makePlayer(playerId, "RB");
        RushingStats lowVolume = new RushingStats();
        lowVolume.setSeasonType("REG");
        lowVolume.setCarries(50);

        when(playerRepository.findByPositionGroup("RB")).thenReturn(List.of(rb));
        when(rushingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("RB");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes all 5 RB career stats
    //YPC = rushing yards / carries — yards from scrimmage = rushing yards + receiving yards
    //Expected: YPC = 2000/500 = 4.00, scrimmage yards = 2000 + 400 = 2400
    void RB_computesAllStatsCorrectly() {
        Player rb = makePlayer(playerId, "RB");
        RushingStats season = new RushingStats();
        season.setSeasonType("REG");
        season.setCarries(500);
        season.setRushingYards(2000);
        season.setRushingTds(15);
        season.setReceivingYards(400);
        season.setBrokenTackles(50);

        when(playerRepository.findByPositionGroup("RB")).thenReturn(List.of(rb));
        when(rushingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(season));

        List<CareerStats> saved = captureStats("RB");

        assertStatValue(saved, "career_rushing_yards",        2000);
        assertStatValue(saved, "career_rushing_tds",          15);
        assertStatValue(saved, "career_yards_per_carry",      new BigDecimal("4.00"));
        assertStatValue(saved, "career_yards_from_scrimmage", 2400);
        assertStatValue(saved, "career_broken_tackles",       50);
    }

    @Test
    //Function that aggregates yards from scrimmage correctly across two seasons
    //Season 1: 1200 rush + 200 rec = 1400 — Season 2: 800 rush + 100 rec = 900 — Total: 2300
    void RB_aggregatesYardsFromScrimmageAcrossSeasons() {
        Player rb = makePlayer(playerId, "RB");

        RushingStats s1 = new RushingStats();
        s1.setSeasonType("REG");
        s1.setCarries(300);
        s1.setRushingYards(1200);
        s1.setRushingTds(10);
        s1.setReceivingYards(200);
        s1.setBrokenTackles(30);

        RushingStats s2 = new RushingStats();
        s2.setSeasonType("REG");
        s2.setCarries(200);
        s2.setRushingYards(800);
        s2.setRushingTds(7);
        s2.setReceivingYards(100);
        s2.setBrokenTackles(20);

        when(playerRepository.findByPositionGroup("RB")).thenReturn(List.of(rb));
        when(rushingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("RB");

        //Total rush yards = 1200 + 800 = 2000; total rec yards = 200 + 100 = 300; scrimmage = 2300
        assertStatValue(saved, "career_yards_from_scrimmage", 2300);
        //YPC = 2000 / 500 = 4.00
        assertStatValue(saved, "career_yards_per_carry", new BigDecimal("4.00"));
    }

    // -------------------------------------------------------------------------
    // WR / TE calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips WRs with fewer than 100 career regular season targets
    void WR_excludesPlayersBelowTargetThreshold() {
        Player wr = makePlayer(playerId, "WR");
        ReceivingStats lowVolume = new ReceivingStats();
        lowVolume.setSeasonType("REG");
        lowVolume.setTargets(30);

        when(playerRepository.findByPositionGroup("WR")).thenReturn(List.of(wr));
        when(receivingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("WR");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes all 5 WR career stats
    //Catch rate = (receptions / targets) * 100 — YPT = yards / targets
    //Expected: catch rate = (80/120)*100 = 66.67%, YPT = 1000/120 = 8.33
    void WR_computesAllStatsCorrectly() {
        Player wr = makePlayer(playerId, "WR");
        ReceivingStats season = new ReceivingStats();
        season.setSeasonType("REG");
        season.setTargets(120);
        season.setReceptions(80);
        season.setReceivingYards(1000);
        season.setReceivingTds(7);
        season.setWopr(BigDecimal.valueOf(0.50));

        when(playerRepository.findByPositionGroup("WR")).thenReturn(List.of(wr));
        when(receivingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(season));

        List<CareerStats> saved = captureStats("WR");

        assertStatValue(saved, "career_receiving_yards",  1000);
        assertStatValue(saved, "career_receiving_tds",    7);
        assertStatValue(saved, "career_catch_rate",       new BigDecimal("66.67"));
        assertStatValue(saved, "career_yards_per_target", new BigDecimal("8.33"));
        assertStatValue(saved, "career_wopr",             new BigDecimal("0.500"));
    }

    @Test
    //Function that computes WOPR as a weighted average by targets
    //Seasons with more targets carry more weight in the average
    //Season 1: WOPR 0.40, 40 targets — Season 2: WOPR 0.60, 60 targets
    //Expected: (0.40*40 + 0.60*60) / 100 = 52/100 = 0.520
    void WR_computesWOPRAsWeightedAvgByTargets() {
        Player wr = makePlayer(playerId, "WR");

        ReceivingStats s1 = new ReceivingStats();
        s1.setSeasonType("REG");
        s1.setTargets(40);
        s1.setReceptions(28);
        s1.setReceivingYards(400);
        s1.setReceivingTds(3);
        s1.setWopr(BigDecimal.valueOf(0.40));

        ReceivingStats s2 = new ReceivingStats();
        s2.setSeasonType("REG");
        s2.setTargets(60);
        s2.setReceptions(42);
        s2.setReceivingYards(700);
        s2.setReceivingTds(5);
        s2.setWopr(BigDecimal.valueOf(0.60));

        when(playerRepository.findByPositionGroup("WR")).thenReturn(List.of(wr));
        when(receivingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("WR");

        assertStatValue(saved, "career_wopr", new BigDecimal("0.520"));
    }

    // -------------------------------------------------------------------------
    // Pass Rush (DE / DT / EDGE) calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips pass rushers with fewer than 32 career regular season games
    void PassRush_excludesPlayersBelowGameThreshold() {
        Player de = makePlayer(playerId, "DE");
        PassRushStats lowVolume = new PassRushStats();
        lowVolume.setSeasonType("REG");
        lowVolume.setGames(10);

        when(playerRepository.findByPositionGroup("DE")).thenReturn(List.of(de));
        when(passRushStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("DE");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes all 4 pass rush career stats
    //Pass rush win rate is a weighted average by snaps — more snaps = more weight
    //Season 1: PRWR 0.10, 200 snaps — Season 2: PRWR 0.20, 400 snaps
    //Expected PRWR: (0.10*200 + 0.20*400) / 600 = 100/600 = 0.17
    void PassRush_computesAllStatsCorrectly() {
        Player de = makePlayer(playerId, "DE");

        PassRushStats s1 = new PassRushStats();
        s1.setSeasonType("REG");
        s1.setGames(16);
        s1.setSacks(BigDecimal.valueOf(5.5));
        s1.setTfl(BigDecimal.valueOf(3.5));
        s1.setPressures(25);
        s1.setPassRushWinRate(BigDecimal.valueOf(0.10));
        s1.setSnaps(200);

        PassRushStats s2 = new PassRushStats();
        s2.setSeasonType("REG");
        s2.setGames(16);
        s2.setSacks(BigDecimal.valueOf(7.0));
        s2.setTfl(BigDecimal.valueOf(4.0));
        s2.setPressures(35);
        s2.setPassRushWinRate(BigDecimal.valueOf(0.20));
        s2.setSnaps(400);

        when(playerRepository.findByPositionGroup("DE")).thenReturn(List.of(de));
        when(passRushStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("DE");

        //5.5 + 7.0 = 12.5 sacks
        assertStatValue(saved, "career_sacks",    new BigDecimal("12.5"));
        //3.5 + 4.0 = 7.5 TFL
        assertStatValue(saved, "career_tfl",      new BigDecimal("7.5"));
        //25 + 35 = 60 pressures
        assertStatValue(saved, "career_pressures", 60);
        //(0.10*200 + 0.20*400) / 600 = 100/600 = 0.1667 → 0.17
        assertStatValue(saved, "career_pass_rush_win_rate", new BigDecimal("0.17"));
    }

    // -------------------------------------------------------------------------
    // LB calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips linebackers with fewer than 32 career regular season games
    void LB_excludesPlayersBelowGameThreshold() {
        Player lb = makePlayer(playerId, "LB");
        LinebackerStats lowVolume = new LinebackerStats();
        lowVolume.setSeasonType("REG");
        lowVolume.setGames(20);

        when(playerRepository.findByPositionGroup("LB")).thenReturn(List.of(lb));
        when(linebackerStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("LB");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly sums all 4 LB career stats across two seasons
    void LB_computesAllStatsCorrectly() {
        Player lb = makePlayer(playerId, "LB");

        LinebackerStats s1 = new LinebackerStats();
        s1.setSeasonType("REG");
        s1.setGames(16);
        s1.setTacklesTotal(80);
        s1.setSacks(BigDecimal.valueOf(4.5));
        s1.setInterceptions(2);
        s1.setPassesDefended(5);

        LinebackerStats s2 = new LinebackerStats();
        s2.setSeasonType("REG");
        s2.setGames(16);
        s2.setTacklesTotal(70);
        s2.setSacks(BigDecimal.valueOf(3.0));
        s2.setInterceptions(3);
        s2.setPassesDefended(6);

        when(playerRepository.findByPositionGroup("LB")).thenReturn(List.of(lb));
        when(linebackerStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("LB");

        assertStatValue(saved, "career_tackles",         150);   // 80 + 70
        assertStatValue(saved, "career_sacks",           new BigDecimal("7.5")); // 4.5 + 3.0
        assertStatValue(saved, "career_interceptions",   5);     // 2 + 3
        assertStatValue(saved, "career_passes_defended", 11);    // 5 + 6
    }

    // -------------------------------------------------------------------------
    // Secondary (CB / S) calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips secondary players with fewer than 32 career regular season games
    void Secondary_excludesPlayersBelowGameThreshold() {
        Player cb = makePlayer(playerId, "CB");
        SecondaryStats lowVolume = new SecondaryStats();
        lowVolume.setSeasonType("REG");
        lowVolume.setGames(20);

        when(playerRepository.findByPositionGroup("CB")).thenReturn(List.of(cb));
        when(secondaryStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(lowVolume));

        careerStatsService.computeCareerStatsForPositionGroup("CB");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes all 3 secondary career stats
    //Passer rating allowed is a weighted average by targets allowed — more targets = more weight
    //Season 1: PRA 95.0, 30 targets — Season 2: PRA 75.0, 70 targets
    //Expected PRA: (95.0*30 + 75.0*70) / 100 = (2850 + 5250) / 100 = 81.00
    void Secondary_computesAllStatsCorrectly() {
        Player cb = makePlayer(playerId, "CB");

        SecondaryStats s1 = new SecondaryStats();
        s1.setSeasonType("REG");
        s1.setGames(16);
        s1.setInterceptions(3);
        s1.setPassesDefended(8);
        s1.setPasserRatingAllowed(BigDecimal.valueOf(95.0));
        s1.setTargetsAllowed(30);

        SecondaryStats s2 = new SecondaryStats();
        s2.setSeasonType("REG");
        s2.setGames(16);
        s2.setInterceptions(5);
        s2.setPassesDefended(12);
        s2.setPasserRatingAllowed(BigDecimal.valueOf(75.0));
        s2.setTargetsAllowed(70);

        when(playerRepository.findByPositionGroup("CB")).thenReturn(List.of(cb));
        when(secondaryStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("CB");

        assertStatValue(saved, "career_interceptions",         8);    // 3 + 5
        assertStatValue(saved, "career_passes_defended",       20);   // 8 + 12
        assertStatValue(saved, "career_passer_rating_allowed", new BigDecimal("81.00"));
    }

    // -------------------------------------------------------------------------
    // Kicker calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips kickers with fewer than 2 seasons of data
    void Kicker_excludesPlayersBelowSeasonThreshold() {
        Player k = makePlayer(playerId, "K");
        KickingStats oneSeason = new KickingStats();
        oneSeason.setSeasonType("REG");
        oneSeason.setFgMade(25);
        oneSeason.setFgAttempted(28);

        when(playerRepository.findByPositionGroup("K")).thenReturn(List.of(k));
        when(kickingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(oneSeason));

        careerStatsService.computeCareerStatsForPositionGroup("K");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes all 3 kicker percentages from career attempt totals
    //FG% = (fgMade / fgAtt) * 100
    //FG50% = (fg50Made / fg50Att) * 100
    //XP% = (xpMade / xpAtt) * 100
    //
    //Season 1: FG 20/25, FG50 4/8, XP 35/36
    //Season 2: FG 10/20, FG50 2/8, XP 28/30
    //Career:  FG 30/45=66.67%, FG50 6/16=37.50%, XP 63/66=95.45%
    void Kicker_computesAllPercentagesCorrectly() {
        Player k = makePlayer(playerId, "K");

        KickingStats s1 = new KickingStats();
        s1.setSeasonType("REG");
        s1.setFgMade(20);
        s1.setFgAttempted(25);
        s1.setFg50PlusMade(4);
        s1.setFg50PlusAttempted(8);
        s1.setXpMade(35);
        s1.setXpAttempted(36);

        KickingStats s2 = new KickingStats();
        s2.setSeasonType("REG");
        s2.setFgMade(10);
        s2.setFgAttempted(20);
        s2.setFg50PlusMade(2);
        s2.setFg50PlusAttempted(8);
        s2.setXpMade(28);
        s2.setXpAttempted(30);

        when(playerRepository.findByPositionGroup("K")).thenReturn(List.of(k));
        when(kickingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("K");

        //30/45 * 100 = 66.67%
        assertStatValue(saved, "career_fg_pct",    new BigDecimal("66.67"));
        //6/16 * 100 = 37.50%
        assertStatValue(saved, "career_fg_50_pct", new BigDecimal("37.50"));
        //63/66 * 100 = 95.45%
        assertStatValue(saved, "career_xp_pct",    new BigDecimal("95.45"));
    }

    // -------------------------------------------------------------------------
    // Punter calculations
    // -------------------------------------------------------------------------

    @Test
    //Function that skips punters with fewer than 2 seasons of data
    void Punter_excludesPlayersBelowSeasonThreshold() {
        Player p = makePlayer(playerId, "P");
        PuntingStats oneSeason = new PuntingStats();
        oneSeason.setSeasonType("REG");
        oneSeason.setPunts(50);

        when(playerRepository.findByPositionGroup("P")).thenReturn(List.of(p));
        when(puntingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(oneSeason));

        careerStatsService.computeCareerStatsForPositionGroup("P");

        verify(careerStatsRepository, never()).save(any());
    }

    @Test
    //Function that correctly computes both punter career stats
    //Net avg is a weighted average by punts — more punts = more weight
    //Inside 20% = (inside20 / total punts) * 100
    //
    //Season 1: netAvg 42.0, 40 punts, 12 inside 20
    //Season 2: netAvg 44.0, 60 punts, 18 inside 20
    //Expected net avg: (42*40 + 44*60) / 100 = 4320/100 = 43.20
    //Expected inside 20%: (12+18) / 100 * 100 = 30.00%
    void Punter_computesAllStatsCorrectly() {
        Player p = makePlayer(playerId, "P");

        PuntingStats s1 = new PuntingStats();
        s1.setSeasonType("REG");
        s1.setPunts(40);
        s1.setPuntsInside20(12);
        s1.setNetAvg(BigDecimal.valueOf(42.0));

        PuntingStats s2 = new PuntingStats();
        s2.setSeasonType("REG");
        s2.setPunts(60);
        s2.setPuntsInside20(18);
        s2.setNetAvg(BigDecimal.valueOf(44.0));

        when(playerRepository.findByPositionGroup("P")).thenReturn(List.of(p));
        when(puntingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(s1, s2));

        List<CareerStats> saved = captureStats("P");

        //(42*40 + 44*60) / 100 = 43.20
        assertStatValue(saved, "career_net_avg",       new BigDecimal("43.20"));
        //30 inside-20 / 100 total punts * 100 = 30.00%
        assertStatValue(saved, "career_inside_20_pct", new BigDecimal("30.00"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    //Builds a Player with the given UUID and position group
    private Player makePlayer(UUID id, String positionGroup) {
        Player p = new Player();
        p.setId(id);
        p.setPositionGroup(positionGroup);
        return p;
    }

    //Builds a single-season PassingStats row with all required fields set
    private PassingStats passingRow(String seasonType, int attempts, int yards, int tds, int completions, double epa) {
        PassingStats s = new PassingStats();
        s.setSeasonType(seasonType);
        s.setAttempts(attempts);
        s.setPassingYards(yards);
        s.setPassingTds(tds);
        s.setCompletions(completions);
        s.setEpaPerDropback(BigDecimal.valueOf(epa));
        return s;
    }

    //Runs computeCareerStatsForPositionGroup and returns all CareerStats rows captured from the save calls
    private List<CareerStats> captureStats(String positionGroup) {
        ArgumentCaptor<CareerStats> captor = ArgumentCaptor.forClass(CareerStats.class);
        careerStatsService.computeCareerStatsForPositionGroup(positionGroup);
        verify(careerStatsRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }

    //Asserts that a specific stat name has the expected integer value in the captured rows
    private void assertStatValue(List<CareerStats> saved, String statName, int expected) {
        CareerStats row = saved.stream()
                .filter(cs -> statName.equals(cs.getStatName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No stat row found for: " + statName));
        assertThat(row.getStatValue())
                .as("stat: " + statName)
                .isEqualByComparingTo(BigDecimal.valueOf(expected));
    }

    //Asserts that a specific stat name has the expected BigDecimal value in the captured rows
    private void assertStatValue(List<CareerStats> saved, String statName, BigDecimal expected) {
        CareerStats row = saved.stream()
                .filter(cs -> statName.equals(cs.getStatName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No stat row found for: " + statName));
        assertThat(row.getStatValue())
                .as("stat: " + statName)
                .isEqualByComparingTo(expected);
    }
}
