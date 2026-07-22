package com.blitz.controllers;

import com.blitz.model.entity.*;
import com.blitz.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
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
    public PassingStats savePassingStats(@RequestBody PassingStats stats) {
        return statsService.savePassingStats(stats);
    }

    @PostMapping("/rushing")
    public RushingStats saveRushingStats(@RequestBody RushingStats stats) {
        return statsService.saveRushingStats(stats);
    }

    @PostMapping("/receiving")
    public ReceivingStats saveReceivingStats(@RequestBody ReceivingStats stats) {
        return statsService.saveReceivingStats(stats);
    }

    @PostMapping("/pass-rush")
    public PassRushStats savePassRushStats(@RequestBody PassRushStats stats) {
        return statsService.savePassRushStats(stats);
    }

    @PostMapping("/linebacker")
    public LinebackerStats saveLinebackerStats(@RequestBody LinebackerStats stats) {
        return statsService.saveLinebackerStats(stats);
    }

    @PostMapping("/secondary")
    public SecondaryStats saveSecondaryStats(@RequestBody SecondaryStats stats) {
        return statsService.saveSecondaryStats(stats);
    }

    @PostMapping("/kicking")
    public KickingStats saveKickingStats(@RequestBody KickingStats stats) {
        return statsService.saveKickingStats(stats);
    }

    @PostMapping("/punting")
    public PuntingStats savePuntingStats(@RequestBody PuntingStats stats) {
        return statsService.savePuntingStats(stats);
    }
}
