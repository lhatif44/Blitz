package com.blitz.service;

import com.blitz.ingestion.client.NflverseClient;
import com.blitz.ingestion.csv.CsvTable;
import com.blitz.ingestion.mapper.*;
import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceImplTest {

    @Mock private NflverseClient nflverseClient;
    @Mock private PlayerRepository playerRepository;
    @Mock private PassingStatsRepository passingStatsRepository;
    @Mock private RushingStatsRepository rushingStatsRepository;
    @Mock private ReceivingStatsRepository receivingStatsRepository;
    @Mock private PassRushStatsRepository passRushStatsRepository;
    @Mock private LinebackerStatsRepository linebackerStatsRepository;
    @Mock private SecondaryStatsRepository secondaryStatsRepository;
    @Mock private KickingStatsRepository kickingStatsRepository;
    @Mock private PuntingStatsRepository puntingStatsRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamService teamService;
    @Mock private StatsService statsService;
    @Mock private CareerStatsService careerStatsService;
    @Mock private PercentileService percentileService;

    private final PositionGroupRouter positionGroupRouter = new PositionGroupRouter();
    private final TeamMapper teamMapper = new TeamMapper();
    private final PlayerMapper playerMapper = new PlayerMapper(positionGroupRouter);
    private final PassingStatsMapper passingStatsMapper = new PassingStatsMapper();
    private final RushingStatsMapper rushingStatsMapper = new RushingStatsMapper();
    private final ReceivingStatsMapper receivingStatsMapper = new ReceivingStatsMapper();
    private final PassRushStatsMapper passRushStatsMapper = new PassRushStatsMapper();
    private final LinebackerStatsMapper linebackerStatsMapper = new LinebackerStatsMapper();
    private final SecondaryStatsMapper secondaryStatsMapper = new SecondaryStatsMapper();
    private final KickingStatsMapper kickingStatsMapper = new KickingStatsMapper();
    private final PuntingStatsMapper puntingStatsMapper = new PuntingStatsMapper();

    private IngestionServiceImpl ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new IngestionServiceImpl(
                nflverseClient, positionGroupRouter,
                teamMapper, playerMapper,
                passingStatsMapper, rushingStatsMapper, receivingStatsMapper, passRushStatsMapper,
                linebackerStatsMapper, secondaryStatsMapper, kickingStatsMapper, puntingStatsMapper,
                playerRepository,
                passingStatsRepository, rushingStatsRepository, receivingStatsRepository, passRushStatsRepository,
                linebackerStatsRepository, secondaryStatsRepository, kickingStatsRepository, puntingStatsRepository,
                playerService, teamService, statsService, careerStatsService, percentileService);

        // sane defaults: no pre-existing rows anywhere unless a test overrides
        lenient().when(playerRepository.findByNflverseId(any())).thenReturn(Optional.empty());
        lenient().when(passingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(any(), any(), any())).thenReturn(List.of());
        lenient().when(playerService.savePlayer(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(teamService.saveTeam(any())).thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(nflverseClient.download(eq("pfr_advstats"), anyString())).thenReturn(csvTable("season,pfr_id"));
        lenient().when(nflverseClient.tryDownload(eq("stats_player"), anyString())).thenReturn(Optional.empty());
    }

    private static CsvTable csvTable(String... lines) {
        String content = String.join("\n", lines);
        return CsvTable.parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    private void stubTeams() {
        when(nflverseClient.download("teams", "teams_colors_logos.csv")).thenReturn(csvTable(
                "team_abbr,team_name,team_conf,team_division,team_color,team_logo_espn",
                "KC,Kansas City Chiefs,AFC,AFC West,#E31837,http://example.com/kc.png",
                "LAC,Los Angeles Chargers,AFC,AFC West,#0080C6,http://example.com/lac.png"));
    }

    private void stubPlayers(String... extraRows) {
        StringBuilder header = new StringBuilder("gsis_id,display_name,position,position_group,pfr_id,draft_team,rookie_season,last_season");
        String[] lines = new String[extraRows.length + 1];
        lines[0] = header.toString();
        System.arraycopy(extraRows, 0, lines, 1, extraRows.length);
        when(nflverseClient.download("players", "players.csv")).thenReturn(csvTable(lines));
    }

    @Test
    void ingestCurrentSeason_savesTeamsAndPlayers_thenRecomputesCareerStatsAndPercentiles() {
        stubTeams();
        stubPlayers("00-0033873,Patrick Mahomes,QB,QB,MahoPa00,KC,2017,");

        ingestionService.ingestCurrentSeason();

        verify(teamService, times(2)).saveTeam(any(Team.class));
        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerService).savePlayer(playerCaptor.capture());
        assertThat(playerCaptor.getValue().getNflverseId()).isEqualTo("00-0033873");
        assertThat(playerCaptor.getValue().getDisplayName()).isEqualTo("Patrick Mahomes");

        InOrder inOrder = inOrder(teamService, playerService, careerStatsService, percentileService);
        inOrder.verify(teamService, atLeastOnce()).saveTeam(any());
        inOrder.verify(playerService).savePlayer(any());
        inOrder.verify(careerStatsService).computeAllCareerStats();
        inOrder.verify(percentileService).computeAllPercentiles();
    }

    @Test
    void ingestCurrentSeason_routesAPassingRowToStatsServiceAndUpsertsByNaturalKey() {
        stubTeams();
        stubPlayers("00-0033873,Patrick Mahomes,QB,QB,MahoPa00,KC,2017,");

        when(nflverseClient.tryDownload(eq("stats_player"), argThat(name -> name.startsWith("stats_player_reg_"))))
                .thenReturn(Optional.of(csvTable(
                        "player_id,position,recent_team,games,completions,attempts,passing_yards,passing_tds,passing_interceptions,sacks_suffered,sack_yards_lost",
                        "00-0033873,QB,KC,16,383,580,5097,50,12,26,156")));

        UUID existingId = UUID.randomUUID();
        PassingStats existing = new PassingStats();
        existing.setId(existingId);
        when(passingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(any(), any(), eq("REG")))
                .thenReturn(List.of(existing));

        ingestionService.ingestCurrentSeason();

        ArgumentCaptor<PassingStats> statsCaptor = ArgumentCaptor.forClass(PassingStats.class);
        verify(statsService, atLeastOnce()).savePassingStats(statsCaptor.capture());

        PassingStats saved = statsCaptor.getAllValues().get(0);
        assertThat(saved.getId()).isEqualTo(existingId); // reused the existing row's ID instead of inserting a duplicate
        assertThat(saved.getPassingYards()).isEqualTo(5097);
        assertThat(saved.getPlayer().getNflverseId()).isEqualTo("00-0033873");
        assertThat(saved.getTeam().getAbbr()).isEqualTo("KC");

        verify(statsService, never()).saveRushingStats(any());
        verify(statsService, never()).saveKickingStats(any());
    }

    @Test
    void ingestCurrentSeason_resolvesHistoricalTeamAbbreviationsToTheCurrentFranchise() {
        stubTeams(); // has LAC, not the historical "SD"
        stubPlayers("00-0011111,Old Charger,WR,WR,OldCh00,,,");

        when(nflverseClient.tryDownload(eq("stats_player"), argThat(name -> name.startsWith("stats_player_reg_"))))
                .thenReturn(Optional.of(csvTable(
                        "player_id,position,recent_team,games,targets,receptions,receiving_yards",
                        "00-0011111,WR,SD,16,100,65,900")));

        ingestionService.ingestCurrentSeason();

        ArgumentCaptor<ReceivingStats> statsCaptor = ArgumentCaptor.forClass(ReceivingStats.class);
        verify(statsService, atLeastOnce()).saveReceivingStats(statsCaptor.capture());
        assertThat(statsCaptor.getAllValues().get(0).getTeam().getAbbr()).isEqualTo("LAC");
    }

    @Test
    void ingestCurrentSeason_mergesAdvancedStatsOnlyForRegularSeasonRows() {
        stubTeams();
        stubPlayers("00-0033873,Patrick Mahomes,QB,QB,MahoPa00,KC,2017,");

        when(nflverseClient.download(eq("pfr_advstats"), eq("advstats_season_pass.csv"))).thenReturn(csvTable(
                "pfr_id,season,pressure_pct",
                "MahoPa00,2024,26.0"));

        when(nflverseClient.tryDownload(eq("stats_player"), contains("_reg_2024"))).thenReturn(Optional.of(csvTable(
                "player_id,position,recent_team,games,completions,attempts",
                "00-0033873,QB,KC,16,383,580")));
        when(nflverseClient.tryDownload(eq("stats_player"), contains("_post_2024"))).thenReturn(Optional.of(csvTable(
                "player_id,position,recent_team,games,completions,attempts",
                "00-0033873,QB,KC,3,70,100")));

        ingestionService.ingestFullHistory();

        ArgumentCaptor<PassingStats> statsCaptor = ArgumentCaptor.forClass(PassingStats.class);
        verify(statsService, atLeastOnce()).savePassingStats(statsCaptor.capture());

        boolean regRowGotAdvancedStats = statsCaptor.getAllValues().stream()
                .anyMatch(s -> "REG".equals(s.getSeasonType()) && s.getSeason() == 2024 && s.getPressureRate() != null);
        boolean postRowGotAdvancedStats = statsCaptor.getAllValues().stream()
                .anyMatch(s -> "POST".equals(s.getSeasonType()) && s.getSeason() == 2024 && s.getPressureRate() != null);

        assertThat(regRowGotAdvancedStats).isTrue();
        assertThat(postRowGotAdvancedStats).isFalse();
    }

    @Test
    void ingestCurrentSeason_skipsStatRowsForPlayersNotInPlayersCsv() {
        stubTeams();
        stubPlayers(); // no players ingested at all

        when(nflverseClient.tryDownload(eq("stats_player"), argThat(name -> name.startsWith("stats_player_reg_"))))
                .thenReturn(Optional.of(csvTable(
                        "player_id,position,recent_team,games,completions,attempts",
                        "00-0099999,QB,KC,16,383,580")));

        ingestionService.ingestCurrentSeason();

        verify(statsService, never()).savePassingStats(any());
    }

    @Test
    void ingestCurrentSeason_skipsPositionsWithNoMatchingStatTable() {
        stubTeams();
        stubPlayers("00-0022222,Some Lineman,OL,OL,SomeLi00,,,");

        when(nflverseClient.tryDownload(eq("stats_player"), argThat(name -> name.startsWith("stats_player_reg_"))))
                .thenReturn(Optional.of(csvTable(
                        "player_id,position,recent_team,games",
                        "00-0022222,OL,KC,16")));

        ingestionService.ingestCurrentSeason();

        verifyNoInteractions(statsService);
    }
}
