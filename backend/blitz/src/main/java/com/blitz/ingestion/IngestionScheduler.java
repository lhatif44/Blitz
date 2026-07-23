package com.blitz.ingestion;

import com.blitz.service.IngestionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// weekly in-season refresh 
@Component
public class IngestionScheduler {

    private final IngestionService ingestionService;

    public IngestionScheduler(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Scheduled(cron = "0 0 6 * * TUE")
    public void refreshCurrentSeason() {
        ingestionService.ingestCurrentSeason();
    }
}
