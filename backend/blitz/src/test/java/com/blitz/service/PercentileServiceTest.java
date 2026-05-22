package com.blitz.service;

import com.blitz.model.entity.CareerPercentile;
import com.blitz.model.entity.CareerStats;
import com.blitz.model.entity.Player;
import com.blitz.repository.CareerPercentileRepository;
import com.blitz.repository.CareerStatsRepository;
import com.blitz.repository.PlayerRepository;
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
public class PercentileServiceTest {

    @Mock private CareerStatsRepository careerStatsRepository;
    @Mock private CareerPercentileRepository careerPercentileRepository;
    @Mock private PlayerRepository playerRepository;

    @InjectMocks
    private PercentileServiceImpl percentileService;

    private UUID playerId;
    private Player qbPlayer;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();

        qbPlayer = new Player();
        qbPlayer.setId(playerId);
        qbPlayer.setNflverseId("qb-001");
        qbPlayer.setDisplayName("Patrick Mahomes");
        qbPlayer.setPositionGroup("QB");
    }

    // -------------------------------------------------------------------------
    // Basic behaviour
    // -------------------------------------------------------------------------

    @Test
    //Function that returns stored percentile rankings from the repository
    void getPercentilesForPlayer_returnsResultsFromRepository() {
        CareerPercentile cp = new CareerPercentile();
        cp.setStatName("career_passing_yards");
        cp.setPercentile(BigDecimal.valueOf(95.0));

        when(careerPercentileRepository.findByPlayerId(playerId)).thenReturn(List.of(cp));

        List<CareerPercentile> result = percentileService.getPercentilesForPlayer(playerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatName()).isEqualTo("career_passing_yards");
        verify(careerPercentileRepository).findByPlayerId(playerId);
    }

    @Test
    //Function that throws when the player does not exist in the database
    void computePercentilesForPlayer_throwsException_whenPlayerNotFound() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> percentileService.computePercentilesForPlayer(playerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    //Function that deletes existing percentile rows before writing new ones
    void computePercentilesForPositionGroup_deletesExistingBeforeRecomputing() {
        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of());

        percentileService.computePercentilesForPositionGroup("QB");

        verify(careerPercentileRepository).deleteByPositionGroup("QB");
    }

    @Test
    //Function that exits without saving anything when there are no career stats to rank
    void computePercentilesForPositionGroup_savesNothing_whenNoCareerStatsExist() {
        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of());

        percentileService.computePercentilesForPositionGroup("QB");

        verify(careerPercentileRepository, never()).save(any());
    }

    @Test
    //Function that triggers a delete-and-recompute for all 12 supported position groups
    void computeAllPercentiles_computesEveryPositionGroup() {
        List<String> allGroups = List.of("QB", "RB", "WR", "TE", "DE", "DT", "EDGE", "LB", "CB", "S", "K", "P");
        for (String group : allGroups) {
            when(careerStatsRepository.findByPositionGroup(group)).thenReturn(List.of());
        }

        percentileService.computeAllPercentiles();

        for (String group : allGroups) {
            verify(careerPercentileRepository).deleteByPositionGroup(group);
        }
    }

    // -------------------------------------------------------------------------
    // Percentile rank formula — PERCENT_RANK logic
    // rank = (number of players with a strictly lower value) / (total players - 1) * 100
    // -------------------------------------------------------------------------

    @Test
    //Two-player scenario: the higher value gets 100th percentile, the lower gets 0th
    //rank(5000) = 1 lower value / (2-1) * 100 = 100.0
    //rank(4000) = 0 lower values / (2-1) * 100 = 0.0
    void percentileRank_twoPlayers_correctHighAndLow() {
        Player qb1 = makePlayer("qb-001", "QB");
        Player qb2 = makePlayer("qb-002", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 5000),
                makeCareerStats(qb2, "QB", "career_passing_yards", 4000)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        assertPercentile(saved, qb1, "career_passing_yards", new BigDecimal("100.0"));
        assertPercentile(saved, qb2, "career_passing_yards", new BigDecimal("0.0"));
    }

    @Test
    //Three-player scenario: the middle player should receive exactly the 50th percentile
    //rank(300) = 2 lower values / (3-1) * 100 = 100.0
    //rank(200) = 1 lower value  / (3-1) * 100 = 50.0
    //rank(100) = 0 lower values / (3-1) * 100 = 0.0
    void percentileRank_threePlayers_middlePlayerGets50thPercentile() {
        Player qb1 = makePlayer("qb-001", "QB");
        Player qb2 = makePlayer("qb-002", "QB");
        Player qb3 = makePlayer("qb-003", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 300),
                makeCareerStats(qb2, "QB", "career_passing_yards", 200),
                makeCareerStats(qb3, "QB", "career_passing_yards", 100)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        assertPercentile(saved, qb1, "career_passing_yards", new BigDecimal("100.0"));
        assertPercentile(saved, qb2, "career_passing_yards", new BigDecimal("50.0"));
        assertPercentile(saved, qb3, "career_passing_yards", new BigDecimal("0.0"));
    }

    @Test
    //Four-player scenario verifies the interpolation at 33.3 and 66.7 percentile
    //rank(400) = 3 / 3 * 100 = 100.0
    //rank(300) = 2 / 3 * 100 = 66.7
    //rank(200) = 1 / 3 * 100 = 33.3
    //rank(100) = 0 / 3 * 100 = 0.0
    void percentileRank_fourPlayers_correctIntermediatePercentiles() {
        Player p1 = makePlayer("p-001", "QB");
        Player p2 = makePlayer("p-002", "QB");
        Player p3 = makePlayer("p-003", "QB");
        Player p4 = makePlayer("p-004", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(p1, "QB", "career_passing_yards", 400),
                makeCareerStats(p2, "QB", "career_passing_yards", 300),
                makeCareerStats(p3, "QB", "career_passing_yards", 200),
                makeCareerStats(p4, "QB", "career_passing_yards", 100)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        assertPercentile(saved, p1, "career_passing_yards", new BigDecimal("100.0"));
        assertPercentile(saved, p2, "career_passing_yards", new BigDecimal("66.7"));
        assertPercentile(saved, p3, "career_passing_yards", new BigDecimal("33.3"));
        assertPercentile(saved, p4, "career_passing_yards", new BigDecimal("0.0"));
    }

    @Test
    //When only one player qualifies, they are by definition at the 100th percentile
    void percentileRank_singlePlayer_receives100thPercentile() {
        Player qb1 = makePlayer("qb-001", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 5000)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        assertPercentile(saved, qb1, "career_passing_yards", new BigDecimal("100.0"));
    }

    @Test
    //When two players have the same career value they are tied
    //Both receive the 0th percentile because neither has a strictly lower value than the other
    void percentileRank_tiedPlayers_bothReceive0thPercentile() {
        Player qb1 = makePlayer("qb-001", "QB");
        Player qb2 = makePlayer("qb-002", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 5000),
                makeCareerStats(qb2, "QB", "career_passing_yards", 5000)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        //Both tied — 0 players rank strictly below 5000, so rank = 0 / (2-1) * 100 = 0.0
        assertPercentile(saved, qb1, "career_passing_yards", new BigDecimal("0.0"));
        assertPercentile(saved, qb2, "career_passing_yards", new BigDecimal("0.0"));
    }

    @Test
    //Each stat is ranked independently — a player can lead in one stat but trail in another
    //qb1 has more yards but fewer TDs — should hold 100th percentile in yards and 0th in TDs
    void percentileRank_eachStatRankedIndependently() {
        Player qb1 = makePlayer("qb-001", "QB");
        Player qb2 = makePlayer("qb-002", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 5000),
                makeCareerStats(qb2, "QB", "career_passing_yards", 4000),
                makeCareerStats(qb1, "QB", "career_passing_tds",   30),
                makeCareerStats(qb2, "QB", "career_passing_tds",   40)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        assertPercentile(saved, qb1, "career_passing_yards", new BigDecimal("100.0"));
        assertPercentile(saved, qb1, "career_passing_tds",   new BigDecimal("0.0"));
        assertPercentile(saved, qb2, "career_passing_yards", new BigDecimal("0.0"));
        assertPercentile(saved, qb2, "career_passing_tds",   new BigDecimal("100.0"));
    }

    @Test
    //The stat value stored on the percentile record must match the value from career_stats
    //This ensures the profile page can display the actual career number alongside the percentile bar
    void percentileRecord_storesCorrectStatValueFromCareerStats() {
        Player qb1 = makePlayer("qb-001", "QB");

        when(careerStatsRepository.findByPositionGroup("QB")).thenReturn(List.of(
                makeCareerStats(qb1, "QB", "career_passing_yards", 52000)
        ));

        List<CareerPercentile> saved = capturePercentiles("QB");

        CareerPercentile record = saved.stream()
                .filter(cp -> cp.getPlayer() == qb1 && "career_passing_yards".equals(cp.getStatName()))
                .findFirst().orElseThrow();

        assertThat(record.getStatValue()).isEqualByComparingTo(BigDecimal.valueOf(52000));
        assertThat(record.getPositionGroup()).isEqualTo("QB");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    //Builds a Player with a unique UUID and a position group
    private Player makePlayer(String nflverseId, String positionGroup) {
        Player p = new Player();
        p.setId(UUID.randomUUID());
        p.setNflverseId(nflverseId);
        p.setPositionGroup(positionGroup);
        return p;
    }

    //Builds a CareerStats row with the given stat name and numeric value
    private CareerStats makeCareerStats(Player player, String positionGroup, String statName, double value) {
        CareerStats cs = new CareerStats();
        cs.setPlayer(player);
        cs.setPositionGroup(positionGroup);
        cs.setStatName(statName);
        cs.setStatValue(BigDecimal.valueOf(value));
        return cs;
    }

    //Runs computePercentilesForPositionGroup and returns all saved CareerPercentile rows
    private List<CareerPercentile> capturePercentiles(String positionGroup) {
        ArgumentCaptor<CareerPercentile> captor = ArgumentCaptor.forClass(CareerPercentile.class);
        percentileService.computePercentilesForPositionGroup(positionGroup);
        verify(careerPercentileRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }

    //Asserts that a specific player has the expected percentile for a given stat
    private void assertPercentile(List<CareerPercentile> saved, Player player, String statName, BigDecimal expected) {
        CareerPercentile record = saved.stream()
                .filter(cp -> cp.getPlayer() == player && statName.equals(cp.getStatName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No percentile record found for player " + player.getNflverseId() + ", stat: " + statName));
        assertThat(record.getPercentile())
                .as("percentile for " + player.getNflverseId() + " / " + statName)
                .isEqualByComparingTo(expected);
    }
}
