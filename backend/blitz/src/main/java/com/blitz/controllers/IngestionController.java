package com.blitz.controllers;

import com.blitz.service.IngestionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/full")
    public void ingestFullHistory() {
        ingestionService.ingestFullHistory();
    }

    @PostMapping("/current")
    public void ingestCurrentSeason() {
        ingestionService.ingestCurrentSeason();
    }
}
