package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.KickingStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.blitz.ingestion.csv.CsvRowTestSupport.row;
import static org.assertj.core.api.Assertions.assertThat;

class KickingStatsMapperTest {

    private final KickingStatsMapper mapper = new KickingStatsMapper();

    private Player player;
    private Team team;

    @BeforeEach
    void setUp() {
        player = new Player();
        team = new Team();
        team.setAbbr("TEN");
    }

    private Map<String, String> baseFixture() {
        Map<String, String> columns = new HashMap<>();
        columns.put("games", "14");
        columns.put("fg_made", "21");
        columns.put("fg_att", "22");
        columns.put("fg_pct", "0.954545454545455");
        columns.put("fg_long", "56");
        columns.put("fg_made_0_19", "0");
        columns.put("fg_missed_0_19", "0");
        columns.put("fg_made_20_29", "5");
        columns.put("fg_missed_20_29", "0");
        columns.put("fg_made_30_39", "2");
        columns.put("fg_missed_30_39", "0");
        columns.put("fg_made_40_49", "8");
        columns.put("fg_missed_40_49", "1");
        columns.put("fg_made_50_59", "6");
        columns.put("fg_missed_50_59", "0");
        columns.put("fg_made_60_", "0");
        columns.put("fg_missed_60_", "0");
        columns.put("fg_blocked_list", "");
        columns.put("pat_made", "25");
        columns.put("pat_att", "25");
        columns.put("pat_pct", "1.0");
        return columns;
    }

    @Test
    void map_withNoBlocks_bucketAttemptedEqualsMadePlusMissed() {
        CsvRow base = row(baseFixture());

        KickingStats stats = mapper.map(base, player, team, 2024, "REG");

        assertThat(stats.getFgMade()).isEqualTo(21);
        assertThat(stats.getFgAttempted()).isEqualTo(22);
        assertThat(stats.getFgPct()).isEqualByComparingTo("95.5");
        assertThat(stats.getFg40To49Made()).isEqualTo(8);
        assertThat(stats.getFg40To49Attempted()).isEqualTo(9);
        assertThat(stats.getFg50PlusMade()).isEqualTo(6);
        assertThat(stats.getFg50PlusAttempted()).isEqualTo(6);
        assertThat(stats.getXpMade()).isEqualTo(25);
        assertThat(stats.getXpPct()).isEqualByComparingTo("100.0");
    }

    @Test
    void map_withBlockedKicks_foldsThemIntoAttemptedNotMade() {
        Map<String, String> columns = baseFixture();
        // a block at 15 (bucket 1-19), one at 45 (bucket 40-49), one at 61 (bucket 50+)
        columns.put("fg_blocked_list", "15;45;61");

        CsvRow base = row(columns);

        KickingStats stats = mapper.map(base, player, team, 2024, "REG");

        assertThat(stats.getFg1To19Made()).isEqualTo(0);
        assertThat(stats.getFg1To19Attempted()).isEqualTo(1);

        assertThat(stats.getFg40To49Made()).isEqualTo(8);
        assertThat(stats.getFg40To49Attempted()).isEqualTo(9 + 1);

        assertThat(stats.getFg50PlusMade()).isEqualTo(6);
        assertThat(stats.getFg50PlusAttempted()).isEqualTo(6 + 1);

        // buckets untouched by a block are unaffected
        assertThat(stats.getFg20To29Attempted()).isEqualTo(5);
        assertThat(stats.getFg30To39Attempted()).isEqualTo(2);
    }

    @Test
    void map_bucketBoundaries_eachBucketsUpperEdgeIsInclusive() {
        Map<String, String> columns = baseFixture();
        columns.put("fg_blocked_list", "19;20;29;30;39;40;49;50");

        CsvRow base = row(columns);

        KickingStats stats = mapper.map(base, player, team, 2024, "REG");

        // 19 -> 1-19, 20 -> 20-29, 29 -> 20-29, 30 -> 30-39, 39 -> 30-39, 40 -> 40-49, 49 -> 40-49, 50 -> 50+
        assertThat(stats.getFg1To19Attempted()).isEqualTo(0 + 1);
        assertThat(stats.getFg20To29Attempted()).isEqualTo(5 + 2);
        assertThat(stats.getFg30To39Attempted()).isEqualTo(2 + 2);
        assertThat(stats.getFg40To49Attempted()).isEqualTo(9 + 2);
        assertThat(stats.getFg50PlusAttempted()).isEqualTo(6 + 1);
    }

    @Test
    void map_withEmptyBlockedList_doesNotThrowAndLeavesBucketsUnchanged() {
        Map<String, String> columns = baseFixture();
        columns.remove("fg_blocked_list"); // column entirely absent, not just blank

        CsvRow base = row(columns);

        KickingStats stats = mapper.map(base, player, team, 2024, "REG");

        assertThat(stats.getFg20To29Attempted()).isEqualTo(5);
    }

    @Test
    void map_withZeroKickerSeason_doesNotThrowOnMissingBucketColumns() {
        // a kicker who never attempted a field goal — bucket columns may be entirely absent
        Map<String, String> columns = new HashMap<>();
        columns.put("games", "4");
        columns.put("fg_made", "0");
        columns.put("fg_att", "0");
        columns.put("pat_made", "0");
        columns.put("pat_att", "0");

        CsvRow base = row(columns);

        KickingStats stats = mapper.map(base, player, team, 2024, "REG");

        assertThat(stats.getFg1To19Attempted()).isEqualTo(0);
        assertThat(stats.getFg50PlusAttempted()).isEqualTo(0);
        assertThat(stats.getFgPct()).isNull();
    }
}
