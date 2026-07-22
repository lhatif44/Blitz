package com.blitz.controllers;

import com.blitz.model.entity.CareerStats;
import com.blitz.service.CareerStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/career-stats")
public class CareerStatsController {

    private final CareerStatsService careerStatsService;

    public CareerStatsController(CareerStatsService careerStatsService) {
        this.careerStatsService = careerStatsService;
    }

    @GetMapping("/{playerId}")
    public List<CareerStats> getCareerStatsForPlayer(@PathVariable UUID playerId) {
        return careerStatsService.getCareerStatsForPlayer(playerId);
    }

    @PostMapping("/{playerId}/recompute")
    public void computeCareerStatsForPlayer(@PathVariable UUID playerId) {
        careerStatsService.computeCareerStatsForPlayer(playerId);
    }

    @PostMapping("/position/{positionGroup}/recompute")
    public void computeCareerStatsForPositionGroup(@PathVariable String positionGroup) {
        careerStatsService.computeCareerStatsForPositionGroup(positionGroup);
    }

    @PostMapping("/recompute-all")
    public void computeAllCareerStats() {
        careerStatsService.computeAllCareerStats();
    }
}
