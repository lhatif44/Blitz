package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.PassingStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.blitz.ingestion.csv.CsvRowTestSupport.row;
import static org.assertj.core.api.Assertions.assertThat;

class PassingStatsMapperTest {

    private final PassingStatsMapper mapper = new PassingStatsMapper();

    private Player player;
    private Team team;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setDisplayName("Patrick Mahomes");
        team = new Team();
        team.setAbbr("KC");
    }

    @Test
    void map_computesRateStatsFromCountingStats() {
        // 2018 Patrick Mahomes-ish line: 383/580, 5097 yards, 50 TD, 12 INT
        CsvRow base = row(Map.of(
                "games", "16",
                "completions", "383",
                "attempts", "580",
                "passing_yards", "5097",
                "passing_tds", "50",
                "passing_interceptions", "12",
                "sacks_suffered", "26",
                "sack_yards_lost", "156",
                "passing_epa", "121.2",
                "passing_cpoe", "4.5"));

        PassingStats stats = mapper.map(base, null, player, team, 2018, "REG");

        assertThat(stats.getPlayer()).isSameAs(player);
        assertThat(stats.getTeam()).isSameAs(team);
        assertThat(stats.getSeason()).isEqualTo(2018);
        assertThat(stats.getSeasonType()).isEqualTo("REG");
        assertThat(stats.getGames()).isEqualTo(16);
        assertThat(stats.getCompletions()).isEqualTo(383);
        assertThat(stats.getAttempts()).isEqualTo(580);
        assertThat(stats.getSacks()).isEqualTo(26);
        assertThat(stats.getSackYardsLost()).isEqualTo(156);

        assertThat(stats.getCompletionPct()).isEqualByComparingTo("66.0");
        assertThat(stats.getYardsPerAttempt()).isEqualByComparingTo("8.79");
        assertThat(stats.getTdPct()).isEqualByComparingTo("8.6");
        assertThat(stats.getIntPct()).isEqualByComparingTo("2.1");
        assertThat(stats.getCpoe()).isEqualByComparingTo("4.5");

        // adjusted yards/attempt = (5097 + 20*50 - 45*12) / 580 = 5557/580
        assertThat(stats.getAdjYardsPerAttempt()).isEqualByComparingTo(
                BigDecimal.valueOf(5097 + 20 * 50 - 45 * 12).divide(BigDecimal.valueOf(580), 2, java.math.RoundingMode.HALF_UP));

        // net yards/attempt = (5097 - 156) / (580 + 26) = 4941/606
        assertThat(stats.getNetYardsPerAttempt()).isEqualByComparingTo(
                BigDecimal.valueOf(5097 - 156).divide(BigDecimal.valueOf(606), 2, java.math.RoundingMode.HALF_UP));

        // epa per dropback = 121.2 / (580 + 26)
        assertThat(stats.getEpaPerDropback()).isEqualByComparingTo(
                new BigDecimal("121.2").divide(BigDecimal.valueOf(606), 4, java.math.RoundingMode.HALF_UP));
    }

    @Test
    void map_passerRating_matchesKnownReferenceValue() {
        // Hand-checked against the standard NFL formula:
        // a = ((24/30)-0.3)*5 = 2.5 -> clamped to 2.375
        // b = ((300/30)-3)*0.25 = 1.75
        // c = (4/30)*20 = 2.667 -> clamped to 2.375
        // d = 2.375-((0/30)*25) = 2.375
        // rating = (2.375+1.75+2.375+2.375)/6*100 = 147.9166... -> 147.9
        CsvRow base = row(Map.of(
                "games", "1",
                "completions", "24",
                "attempts", "30",
                "passing_yards", "300",
                "passing_tds", "4",
                "passing_interceptions", "0",
                "sacks_suffered", "0",
                "sack_yards_lost", "0"));

        PassingStats stats = mapper.map(base, null, player, team, 2024, "REG");

        assertThat(stats.getPasserRating()).isEqualByComparingTo("147.9");
    }

    @Test
    void map_withoutAdvRow_leavesAdvancedFieldsNull() {
        CsvRow base = row(Map.of(
                "games", "16",
                "completions", "383",
                "attempts", "580",
                "passing_yards", "5097",
                "passing_tds", "50",
                "passing_interceptions", "12",
                "sacks_suffered", "26",
                "sack_yards_lost", "156"));

        PassingStats stats = mapper.map(base, null, player, team, 2018, "REG");

        assertThat(stats.getPressureRate()).isNull();
        assertThat(stats.getTimeToThrowAvg()).isNull();
        assertThat(stats.getOnTargetPct()).isNull();
        assertThat(stats.getBadThrowPct()).isNull();
        assertThat(stats.getBlitzedPct()).isNull();
        assertThat(stats.getScrambles()).isNull();
        assertThat(stats.getAdot()).isNull();
        assertThat(stats.getSuccessRate()).isNull();
        assertThat(stats.getQbr()).isNull();
    }

    @Test
    void map_withAdvRow_mergesAdvancedStats() {
        CsvRow base = row(Map.of(
                "games", "16",
                "completions", "383",
                "attempts", "580",
                "passing_yards", "5097",
                "passing_tds", "50",
                "passing_interceptions", "12",
                "sacks_suffered", "26",
                "sack_yards_lost", "156"));

        CsvRow adv = row(Map.ofEntries(
                Map.entry("pressure_pct", "26.0"),
                Map.entry("pocket_time", "2.5"),
                Map.entry("on_tgt_pct", "75.4"),
                Map.entry("bad_throw_pct", "15.2"),
                Map.entry("times_blitzed", "145"),
                Map.entry("pass_attempts", "580"),
                Map.entry("scrambles", "18")));

        PassingStats stats = mapper.map(base, adv, player, team, 2018, "REG");

        assertThat(stats.getPressureRate()).isEqualByComparingTo("26.0");
        assertThat(stats.getTimeToThrowAvg()).isEqualByComparingTo("2.5");
        assertThat(stats.getOnTargetPct()).isEqualByComparingTo("75.4");
        assertThat(stats.getBadThrowPct()).isEqualByComparingTo("15.2");
        assertThat(stats.getBlitzedPct()).isEqualByComparingTo(
                BigDecimal.valueOf(145).divide(BigDecimal.valueOf(580), 5, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, java.math.RoundingMode.HALF_UP));
        assertThat(stats.getScrambles()).isEqualTo(18);
    }

    @Test
    void map_withZeroAttempts_leavesRateStatsNullInsteadOfDividingByZero() {
        CsvRow base = row(Map.of(
                "games", "1",
                "completions", "0",
                "attempts", "0",
                "passing_yards", "0",
                "passing_tds", "0",
                "passing_interceptions", "0",
                "sacks_suffered", "0",
                "sack_yards_lost", "0"));

        PassingStats stats = mapper.map(base, null, player, team, 2018, "REG");

        assertThat(stats.getCompletionPct()).isNull();
        assertThat(stats.getYardsPerAttempt()).isNull();
        assertThat(stats.getPasserRating()).isNull();
        assertThat(stats.getAdjYardsPerAttempt()).isNull();
        assertThat(stats.getNetYardsPerAttempt()).isNull();
        assertThat(stats.getEpaPerDropback()).isNull();
    }
}
