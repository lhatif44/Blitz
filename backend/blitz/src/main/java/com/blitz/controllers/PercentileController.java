package com.blitz.controllers;

import com.blitz.model.entity.CareerPercentile;
import com.blitz.service.PercentileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/percentiles")
public class PercentileController {

    private final PercentileService percentileService;

    public PercentileController(PercentileService percentileService) {
        this.percentileService = percentileService;
    }

    @GetMapping("/{playerId}")
    public List<CareerPercentile> getPercentilesForPlayer(@PathVariable UUID playerId) {
        return percentileService.getPercentilesForPlayer(playerId);
    }

    @PostMapping("/{playerId}/recompute")
    public void computePercentilesForPlayer(@PathVariable UUID playerId) {
        percentileService.computePercentilesForPlayer(playerId);
    }

    @PostMapping("/position/{positionGroup}/recompute")
    public void computePercentilesForPositionGroup(@PathVariable String positionGroup) {
        percentileService.computePercentilesForPositionGroup(positionGroup);
    }

    @PostMapping("/recompute-all")
    public void computeAllPercentiles() {
        percentileService.computeAllPercentiles();
    }
}
