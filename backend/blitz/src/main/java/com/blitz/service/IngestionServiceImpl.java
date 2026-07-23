package com.blitz.service;

import com.blitz.ingestion.client.NflverseClient;
import com.blitz.ingestion.csv.CsvRow;
import com.blitz.ingestion.csv.CsvTable;
import com.blitz.ingestion.mapper.*;
import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Orchestrates pulling data from nflverse-data and loading it through the existing
// service layer. Deliberately NOT @Transactional at this level — each underlying
// save (PlayerService/TeamService/StatsService) already commits in its own
// transaction, so a full-history run that fails partway through (network blip,
// etc.) keeps whatever it already ingested instead of rolling back everything.
@Service
public class IngestionServiceImpl implements IngestionService {

    private static final int FIRST_SEASON = 1999;
    private static final int FIRST_ADVANCED_STATS_SEASON = 2018;

    // nflverse uses the team abbreviation as it was in that season, not the franchise's
    // current one — relocated franchises need normalizing before a teamsByAbbr lookup
    private static final Map<String, String> HISTORICAL_TEAM_ALIASES = Map.of(
            "SD", "LAC",
            "STL", "LAR",
            "OAK", "LV");

    private final NflverseClient nflverseClient;
    private final PositionGroupRouter positionGroupRouter;

    private final TeamMapper teamMapper;
    private final PlayerMapper playerMapper;
    private final PassingStatsMapper passingStatsMapper;
    private final RushingStatsMapper rushingStatsMapper;
    private final ReceivingStatsMapper receivingStatsMapper;
    private final PassRushStatsMapper passRushStatsMapper;
    private final LinebackerStatsMapper linebackerStatsMapper;
    private final SecondaryStatsMapper secondaryStatsMapper;
    private final KickingStatsMapper kickingStatsMapper;
    private final PuntingStatsMapper puntingStatsMapper;

    private final PlayerRepository playerRepository;
    private final PassingStatsRepository passingStatsRepository;
    private final RushingStatsRepository rushingStatsRepository;
    private final ReceivingStatsRepository receivingStatsRepository;
    private final PassRushStatsRepository passRushStatsRepository;
    private final LinebackerStatsRepository linebackerStatsRepository;
    private final SecondaryStatsRepository secondaryStatsRepository;
    private final KickingStatsRepository kickingStatsRepository;
    private final PuntingStatsRepository puntingStatsRepository;

    private final PlayerService playerService;
    private final TeamService teamService;
    private final StatsService statsService;
    private final CareerStatsService careerStatsService;
    private final PercentileService percentileService;

    public IngestionServiceImpl(
            NflverseClient nflverseClient,
            PositionGroupRouter positionGroupRouter,
            TeamMapper teamMapper,
            PlayerMapper playerMapper,
            PassingStatsMapper passingStatsMapper,
            RushingStatsMapper rushingStatsMapper,
            ReceivingStatsMapper receivingStatsMapper,
            PassRushStatsMapper passRushStatsMapper,
            LinebackerStatsMapper linebackerStatsMapper,
            SecondaryStatsMapper secondaryStatsMapper,
            KickingStatsMapper kickingStatsMapper,
            PuntingStatsMapper puntingStatsMapper,
            PlayerRepository playerRepository,
            PassingStatsRepository passingStatsRepository,
            RushingStatsRepository rushingStatsRepository,
            ReceivingStatsRepository receivingStatsRepository,
            PassRushStatsRepository passRushStatsRepository,
            LinebackerStatsRepository linebackerStatsRepository,
            SecondaryStatsRepository secondaryStatsRepository,
            KickingStatsRepository kickingStatsRepository,
            PuntingStatsRepository puntingStatsRepository,
            PlayerService playerService,
            TeamService teamService,
            StatsService statsService,
            CareerStatsService careerStatsService,
            PercentileService percentileService) {
        this.nflverseClient = nflverseClient;
        this.positionGroupRouter = positionGroupRouter;
        this.teamMapper = teamMapper;
        this.playerMapper = playerMapper;
        this.passingStatsMapper = passingStatsMapper;
        this.rushingStatsMapper = rushingStatsMapper;
        this.receivingStatsMapper = receivingStatsMapper;
        this.passRushStatsMapper = passRushStatsMapper;
        this.linebackerStatsMapper = linebackerStatsMapper;
        this.secondaryStatsMapper = secondaryStatsMapper;
        this.kickingStatsMapper = kickingStatsMapper;
        this.puntingStatsMapper = puntingStatsMapper;
        this.playerRepository = playerRepository;
        this.passingStatsRepository = passingStatsRepository;
        this.rushingStatsRepository = rushingStatsRepository;
        this.receivingStatsRepository = receivingStatsRepository;
        this.passRushStatsRepository = passRushStatsRepository;
        this.linebackerStatsRepository = linebackerStatsRepository;
        this.secondaryStatsRepository = secondaryStatsRepository;
        this.kickingStatsRepository = kickingStatsRepository;
        this.puntingStatsRepository = puntingStatsRepository;
        this.playerService = playerService;
        this.teamService = teamService;
        this.statsService = statsService;
        this.careerStatsService = careerStatsService;
        this.percentileService = percentileService;
    }

    @Override
    public void ingestFullHistory() {
        Map<String, Team> teamsByAbbr = ingestTeams();
        PlayerContext playerContext = ingestPlayers(teamsByAbbr);
        AdvancedStatsIndex advancedStats = loadAdvancedStats();

        int latestSeason = currentSeason() + 1;
        for (int season = FIRST_SEASON; season <= latestSeason; season++) {
            ingestSeason(season, playerContext, teamsByAbbr, advancedStats);
        }

        careerStatsService.computeAllCareerStats();
        percentileService.computeAllPercentiles();
    }

    @Override
    public void ingestCurrentSeason() {
        Map<String, Team> teamsByAbbr = ingestTeams();
        PlayerContext playerContext = ingestPlayers(teamsByAbbr);
        AdvancedStatsIndex advancedStats = loadAdvancedStats();

        int season = currentSeason();
        ingestSeason(season, playerContext, teamsByAbbr, advancedStats);
        ingestSeason(season + 1, playerContext, teamsByAbbr, advancedStats); // in case the new season has already started

        careerStatsService.computeAllCareerStats();
        percentileService.computeAllPercentiles();
    }

    // best-effort guess at the most recently completed/active NFL season — the season
    // "turns over" around September when the new one kicks off
    private int currentSeason() {
        LocalDate today = LocalDate.now();
        return today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
    }

    private Map<String, Team> ingestTeams() {
        CsvTable table = nflverseClient.download("teams", "teams_colors_logos.csv");
        Map<String, Team> teamsByAbbr = new HashMap<>();
        for (CsvRow row : table.rows()) {
            Team team = teamMapper.map(row);
            Team saved = teamService.saveTeam(team); // abbr is the PK — this naturally upserts
            teamsByAbbr.put(saved.getAbbr(), saved);
        }
        return teamsByAbbr;
    }

    private PlayerContext ingestPlayers(Map<String, Team> teamsByAbbr) {
        CsvTable table = nflverseClient.download("players", "players.csv");
        Map<String, Player> playersByNflverseId = new HashMap<>();
        Map<String, String> pfrIdByNflverseId = new HashMap<>();

        for (CsvRow row : table.rows()) {
            Player player = playerMapper.map(row, abbr -> resolveTeam(teamsByAbbr, abbr));
            if (player.getNflverseId() == null) {
                continue;
            }

            playerRepository.findByNflverseId(player.getNflverseId())
                    .ifPresent(existing -> player.setId(existing.getId()));

            Player saved = playerService.savePlayer(player);
            playersByNflverseId.put(saved.getNflverseId(), saved);

            String pfrId = row.getString("pfr_id");
            if (pfrId != null) {
                pfrIdByNflverseId.put(saved.getNflverseId(), pfrId);
            }
        }

        return new PlayerContext(playersByNflverseId, pfrIdByNflverseId);
    }

    private AdvancedStatsIndex loadAdvancedStats() {
        return new AdvancedStatsIndex(
                indexBySeasonAndPfrId(nflverseClient.download("pfr_advstats", "advstats_season_pass.csv")),
                indexBySeasonAndPfrId(nflverseClient.download("pfr_advstats", "advstats_season_rush.csv")),
                indexBySeasonAndPfrId(nflverseClient.download("pfr_advstats", "advstats_season_rec.csv")),
                indexBySeasonAndPfrId(nflverseClient.download("pfr_advstats", "advstats_season_def.csv")));
    }

    private Map<String, Map<Integer, CsvRow>> indexBySeasonAndPfrId(CsvTable table) {
        // TODO: a player traded mid-season gets multiple PFR rows for the same (pfrId, season) —
        //  one per team, plus a multi-team summary row (team column "2TM"/"3TM"). This silently
        //  keeps whichever row appears last in the file. Should prefer the multi-team summary row
        //  on collision instead, since our schema stores one row per player per season (not per team).
        Map<String, Map<Integer, CsvRow>> index = new HashMap<>();
        for (CsvRow row : table.rows()) {
            String pfrId = row.getString("pfr_id");
            Integer season = row.getInt("season");
            if (pfrId == null || season == null) {
                continue;
            }
            index.computeIfAbsent(pfrId, key -> new HashMap<>()).put(season, row);
        }
        return index;
    }

    private void ingestSeason(int season, PlayerContext playerContext, Map<String, Team> teamsByAbbr, AdvancedStatsIndex advancedStats) {
        for (String seasonType : List.of("REG", "POST")) {
            String fileName = "stats_player_" + (seasonType.equals("REG") ? "reg" : "post") + "_" + season + ".csv";
            nflverseClient.tryDownload("stats_player", fileName)
                    .ifPresent(table -> table.rows().forEach(row ->
                            processStatRow(row, season, seasonType, playerContext, teamsByAbbr, advancedStats)));
        }
    }

    private void processStatRow(CsvRow row, int season, String seasonType, PlayerContext playerContext,
                                 Map<String, Team> teamsByAbbr, AdvancedStatsIndex advancedStats) {
        String nflverseId = row.getString("player_id");
        Player player = nflverseId != null ? playerContext.playersByNflverseId().get(nflverseId) : null;
        if (player == null) {
            return; // player not in players.csv — skip
        }

        String teamAbbr = row.getString("recent_team");
        Team team = resolveTeam(teamsByAbbr, teamAbbr);

        PositionGroupRouter.Route route = positionGroupRouter.route(row.getString("position"));
        boolean advancedEligible = "REG".equals(seasonType) && season >= FIRST_ADVANCED_STATS_SEASON;
        String pfrId = playerContext.pfrIdByNflverseId().get(nflverseId);

        switch (route.category()) {
            case PASSING -> upsertPassing(passingStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.pass(), pfrId, season) : null, player, team, season, seasonType));
            case RUSHING -> upsertRushing(rushingStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.rush(), pfrId, season) : null, player, team, season, seasonType));
            case RECEIVING -> upsertReceiving(receivingStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.rec(), pfrId, season) : null, player, team, season, seasonType));
            case PASS_RUSH -> upsertPassRush(passRushStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.def(), pfrId, season) : null, player, team, season, seasonType));
            case LINEBACKER -> upsertLinebacker(linebackerStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.def(), pfrId, season) : null, player, team, season, seasonType));
            case SECONDARY -> upsertSecondary(secondaryStatsMapper.map(row,
                    advancedEligible ? lookup(advancedStats.def(), pfrId, season) : null, player, team, season, seasonType));
            case KICKING -> upsertKicking(kickingStatsMapper.map(row, player, team, season, seasonType));
            case PUNTING -> upsertPunting(puntingStatsMapper.map(row, player, team, season, seasonType));
            case NONE -> {
                // position has no matching stat table (OL, LS, ...) — nothing to ingest
            }
        }
    }

    private Team resolveTeam(Map<String, Team> teamsByAbbr, String abbr) {
        if (abbr == null) {
            return null;
        }
        String normalized = HISTORICAL_TEAM_ALIASES.getOrDefault(abbr, abbr);
        return teamsByAbbr.get(normalized);
    }

    private CsvRow lookup(Map<String, Map<Integer, CsvRow>> index, String pfrId, int season) {
        if (pfrId == null) {
            return null;
        }
        Map<Integer, CsvRow> bySeason = index.get(pfrId);
        return bySeason == null ? null : bySeason.get(season);
    }

    // --- upsert helpers: reuse the existing row's ID (found by player/season/seasonType)
    // so re-running ingestion updates rows in place instead of inserting duplicates ---

    private void upsertPassing(PassingStats stats) {
        passingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.savePassingStats(stats);
    }

    private void upsertRushing(RushingStats stats) {
        rushingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.saveRushingStats(stats);
    }

    private void upsertReceiving(ReceivingStats stats) {
        receivingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.saveReceivingStats(stats);
    }

    private void upsertPassRush(PassRushStats stats) {
        passRushStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.savePassRushStats(stats);
    }

    private void upsertLinebacker(LinebackerStats stats) {
        linebackerStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.saveLinebackerStats(stats);
    }

    private void upsertSecondary(SecondaryStats stats) {
        secondaryStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.saveSecondaryStats(stats);
    }

    private void upsertKicking(KickingStats stats) {
        kickingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.saveKickingStats(stats);
    }

    private void upsertPunting(PuntingStats stats) {
        puntingStatsRepository.findByPlayerIdAndSeasonAndSeasonType(stats.getPlayer().getId(), stats.getSeason(), stats.getSeasonType())
                .stream().findFirst().ifPresent(existing -> stats.setId(existing.getId()));
        statsService.savePuntingStats(stats);
    }

    private record PlayerContext(Map<String, Player> playersByNflverseId, Map<String, String> pfrIdByNflverseId) {
    }

    private record AdvancedStatsIndex(
            Map<String, Map<Integer, CsvRow>> pass,
            Map<String, Map<Integer, CsvRow>> rush,
            Map<String, Map<Integer, CsvRow>> rec,
            Map<String, Map<Integer, CsvRow>> def) {
    }
}
