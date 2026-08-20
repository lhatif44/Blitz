package com.blitz.controllers;

import com.blitz.dto.*;
import com.blitz.model.entity.*;
import com.blitz.service.PlayerService;
import com.blitz.service.StatsService;
import com.blitz.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final PlayerService playerService;
    private final TeamService teamService;

    public StatsController(StatsService statsService, PlayerService playerService, TeamService teamService) {
        this.statsService = statsService;
        this.playerService = playerService;
        this.teamService = teamService;
    }

    @GetMapping("/passing/{playerId}")
    public List<PassingStats> getPassingStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getPassingStats(playerId, seasonType);
    }

    @GetMapping("/rushing/{playerId}")
    public List<RushingStats> getRushingStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getRushingStats(playerId, seasonType);
    }

    @GetMapping("/receiving/{playerId}")
    public List<ReceivingStats> getReceivingStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getReceivingStats(playerId, seasonType);
    }

    @GetMapping("/pass-rush/{playerId}")
    public List<PassRushStats> getPassRushStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getPassRushStats(playerId, seasonType);
    }

    @GetMapping("/linebacker/{playerId}")
    public List<LinebackerStats> getLinebackerStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getLinebackerStats(playerId, seasonType);
    }

    @GetMapping("/secondary/{playerId}")
    public List<SecondaryStats> getSecondaryStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getSecondaryStats(playerId, seasonType);
    }

    @GetMapping("/kicking/{playerId}")
    public List<KickingStats> getKickingStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getKickingStats(playerId, seasonType);
    }

    @GetMapping("/punting/{playerId}")
    public List<PuntingStats> getPuntingStats(@PathVariable UUID playerId, @RequestParam(required = false) String seasonType) {
        return statsService.getPuntingStats(playerId, seasonType);
    }

    @PostMapping("/passing")
    public PassingStats savePassingStats(@Valid @RequestBody PassingStatsRequest request) {
        PassingStats stats = new PassingStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setCompletions(request.completions());
        stats.setAttempts(request.attempts());
        stats.setPassingYards(request.passingYards());
        stats.setPassingTds(request.passingTds());
        stats.setInterceptions(request.interceptions());
        stats.setSacks(request.sacks());
        stats.setSackYardsLost(request.sackYardsLost());
        stats.setCompletionPct(request.completionPct());
        stats.setYardsPerAttempt(request.yardsPerAttempt());
        stats.setAdjYardsPerAttempt(request.adjYardsPerAttempt());
        stats.setNetYardsPerAttempt(request.netYardsPerAttempt());
        stats.setTdPct(request.tdPct());
        stats.setIntPct(request.intPct());
        stats.setPasserRating(request.passerRating());
        stats.setQbr(request.qbr());
        stats.setCpoe(request.cpoe());
        stats.setEpaPerDropback(request.epaPerDropback());
        stats.setSuccessRate(request.successRate());
        stats.setPressureRate(request.pressureRate());
        stats.setTimeToThrowAvg(request.timeToThrowAvg());
        stats.setAdot(request.adot());
        stats.setOnTargetPct(request.onTargetPct());
        stats.setBadThrowPct(request.badThrowPct());
        stats.setBlitzedPct(request.blitzedPct());
        stats.setScrambles(request.scrambles());
        stats.setScrambleYards(request.scrambleYards());
        stats.setScrambleTds(request.scrambleTds());
        return statsService.savePassingStats(stats);
    }

    @PostMapping("/rushing")
    public RushingStats saveRushingStats(@Valid @RequestBody RushingStatsRequest request) {
        RushingStats stats = new RushingStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setCarries(request.carries());
        stats.setRushingYards(request.rushingYards());
        stats.setRushingTds(request.rushingTds());
        stats.setFumbles(request.fumbles());
        stats.setFumblesLost(request.fumblesLost());
        stats.setRushingFirstDowns(request.rushingFirstDowns());
        stats.setYardsPerCarry(request.yardsPerCarry());
        stats.setSuccessRate(request.successRate());
        stats.setRushingEpa(request.rushingEpa());
        stats.setYardsAfterContact(request.yardsAfterContact());
        stats.setBrokenTackles(request.brokenTackles());
        stats.setYacPerCarry(request.yacPerCarry());
        stats.setStuffRate(request.stuffRate());
        stats.setRuns10Plus(request.runs10Plus());
        stats.setRuns20Plus(request.runs20Plus());
        stats.setTargets(request.targets());
        stats.setReceptions(request.receptions());
        stats.setReceivingYards(request.receivingYards());
        stats.setReceivingTds(request.receivingTds());
        stats.setYardsPerReception(request.yardsPerReception());
        return statsService.saveRushingStats(stats);
    }

    @PostMapping("/receiving")
    public ReceivingStats saveReceivingStats(@Valid @RequestBody ReceivingStatsRequest request) {
        ReceivingStats stats = new ReceivingStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setTargets(request.targets());
        stats.setReceptions(request.receptions());
        stats.setReceivingYards(request.receivingYards());
        stats.setReceivingTds(request.receivingTds());
        stats.setDrops(request.drops());
        stats.setFumbles(request.fumbles());
        stats.setCatchRate(request.catchRate());
        stats.setYardsPerTarget(request.yardsPerTarget());
        stats.setYardsPerReception(request.yardsPerReception());
        stats.setDropRate(request.dropRate());
        stats.setAdot(request.adot());
        stats.setAirYards(request.airYards());
        stats.setYardsAfterCatch(request.yardsAfterCatch());
        stats.setYacPerReception(request.yacPerReception());
        stats.setTargetShare(request.targetShare());
        stats.setAirYardsShare(request.airYardsShare());
        stats.setWopr(request.wopr());
        stats.setEpaPerTarget(request.epaPerTarget());
        stats.setSeparationAvg(request.separationAvg());
        stats.setContestedCatchRate(request.contestedCatchRate());
        stats.setRedZoneTargets(request.redZoneTargets());
        stats.setEndZoneTargets(request.endZoneTargets());
        return statsService.saveReceivingStats(stats);
    }

    @PostMapping("/pass-rush")
    public PassRushStats savePassRushStats(@Valid @RequestBody PassRushStatsRequest request) {
        PassRushStats stats = new PassRushStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setSnaps(request.snaps());
        stats.setTacklesSolo(request.tacklesSolo());
        stats.setTacklesAssist(request.tacklesAssist());
        stats.setTacklesTotal(request.tacklesTotal());
        stats.setSacks(request.sacks());
        stats.setTfl(request.tfl());
        stats.setQbHits(request.qbHits());
        stats.setQbHurries(request.qbHurries());
        stats.setPressures(request.pressures());
        stats.setPassRushWinRate(request.passRushWinRate());
        stats.setPressureRate(request.pressureRate());
        stats.setSackPct(request.sackPct());
        stats.setRunStops(request.runStops());
        stats.setRunStopPct(request.runStopPct());
        stats.setStuffs(request.stuffs());
        stats.setForcedFumbles(request.forcedFumbles());
        stats.setFumbleRecoveries(request.fumbleRecoveries());
        stats.setFumbleReturnTds(request.fumbleReturnTds());
        return statsService.savePassRushStats(stats);
    }

    @PostMapping("/linebacker")
    public LinebackerStats saveLinebackerStats(@Valid @RequestBody LinebackerStatsRequest request) {
        LinebackerStats stats = new LinebackerStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setSnaps(request.snaps());
        stats.setTacklesSolo(request.tacklesSolo());
        stats.setTacklesAssist(request.tacklesAssist());
        stats.setTacklesTotal(request.tacklesTotal());
        stats.setSacks(request.sacks());
        stats.setTfl(request.tfl());
        stats.setQbHits(request.qbHits());
        stats.setTargetsAllowed(request.targetsAllowed());
        stats.setReceptionsAllowed(request.receptionsAllowed());
        stats.setYardsAllowed(request.yardsAllowed());
        stats.setTdsAllowed(request.tdsAllowed());
        stats.setPasserRatingAllowed(request.passerRatingAllowed());
        stats.setCompletionPctAllowed(request.completionPctAllowed());
        stats.setInterceptions(request.interceptions());
        stats.setIntYards(request.intYards());
        stats.setIntTds(request.intTds());
        stats.setPassesDefended(request.passesDefended());
        stats.setForcedFumbles(request.forcedFumbles());
        stats.setFumbleRecoveries(request.fumbleRecoveries());
        return statsService.saveLinebackerStats(stats);
    }

    @PostMapping("/secondary")
    public SecondaryStats saveSecondaryStats(@Valid @RequestBody SecondaryStatsRequest request) {
        SecondaryStats stats = new SecondaryStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setSnaps(request.snaps());
        stats.setSnapsInCoverage(request.snapsInCoverage());
        stats.setTacklesSolo(request.tacklesSolo());
        stats.setTacklesAssist(request.tacklesAssist());
        stats.setTacklesTotal(request.tacklesTotal());
        stats.setTargetsAllowed(request.targetsAllowed());
        stats.setReceptionsAllowed(request.receptionsAllowed());
        stats.setYardsAllowed(request.yardsAllowed());
        stats.setTdsAllowed(request.tdsAllowed());
        stats.setPasserRatingAllowed(request.passerRatingAllowed());
        stats.setCompletionPctAllowed(request.completionPctAllowed());
        stats.setYardsPerTargetAllowed(request.yardsPerTargetAllowed());
        stats.setBurnedRate(request.burnedRate());
        stats.setInterceptions(request.interceptions());
        stats.setIntYards(request.intYards());
        stats.setIntReturnTds(request.intReturnTds());
        stats.setPassesDefended(request.passesDefended());
        stats.setForcedFumbles(request.forcedFumbles());
        stats.setRunStops(request.runStops());
        stats.setRunStopPct(request.runStopPct());
        return statsService.saveSecondaryStats(stats);
    }

    @PostMapping("/kicking")
    public KickingStats saveKickingStats(@Valid @RequestBody KickingStatsRequest request) {
        KickingStats stats = new KickingStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setFgMade(request.fgMade());
        stats.setFgAttempted(request.fgAttempted());
        stats.setFgPct(request.fgPct());
        stats.setFg1To19Made(request.fg1To19Made());
        stats.setFg1To19Attempted(request.fg1To19Attempted());
        stats.setFg20To29Made(request.fg20To29Made());
        stats.setFg20To29Attempted(request.fg20To29Attempted());
        stats.setFg30To39Made(request.fg30To39Made());
        stats.setFg30To39Attempted(request.fg30To39Attempted());
        stats.setFg40To49Made(request.fg40To49Made());
        stats.setFg40To49Attempted(request.fg40To49Attempted());
        stats.setFg50PlusMade(request.fg50PlusMade());
        stats.setFg50PlusAttempted(request.fg50PlusAttempted());
        stats.setFgLong(request.fgLong());
        stats.setXpMade(request.xpMade());
        stats.setXpAttempted(request.xpAttempted());
        stats.setXpPct(request.xpPct());
        stats.setKickoffs(request.kickoffs());
        stats.setTouchbacks(request.touchbacks());
        stats.setTouchbackPct(request.touchbackPct());
        return statsService.saveKickingStats(stats);
    }

    @PostMapping("/punting")
    public PuntingStats savePuntingStats(@Valid @RequestBody PuntingStatsRequest request) {
        PuntingStats stats = new PuntingStats();
        stats.setId(request.id());
        stats.setPlayer(resolvePlayer(request.playerId()));
        stats.setTeam(resolveTeam(request.teamAbbr()));
        stats.setSeason(request.season());
        stats.setSeasonType(request.seasonType());
        stats.setGames(request.games());
        stats.setPunts(request.punts());
        stats.setPuntYards(request.puntYards());
        stats.setGrossAvg(request.grossAvg());
        stats.setNetAvg(request.netAvg());
        stats.setPuntsInside20(request.puntsInside20());
        stats.setTouchbacks(request.touchbacks());
        stats.setLongPunt(request.longPunt());
        stats.setFairCatchesForced(request.fairCatchesForced());
        return statsService.savePuntingStats(stats);
    }

    private Player resolvePlayer(UUID playerId) {
        return playerService.getPlayerById(playerId);
    }

    private Team resolveTeam(String teamAbbr) {
        return teamAbbr != null ? teamService.getTeamByAbbr(teamAbbr) : null;
    }
}
