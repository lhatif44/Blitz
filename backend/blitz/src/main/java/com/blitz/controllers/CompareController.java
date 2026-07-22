package com.blitz.controllers;

import com.blitz.service.CompareService;
import com.blitz.service.CompareService.ComparisonResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/compare")
public class CompareController {

    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @GetMapping
    public ComparisonResult comparePlayers(
        @RequestParam UUID player1,
        @RequestParam UUID player2,
        @RequestParam(required = false) String seasonType
    ) {
        return compareService.comparePlayers(player1, player2, seasonType);
    }
}
