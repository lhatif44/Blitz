package com.blitz.service;

public interface IngestionService {

    // full historical backfill — every season nflverse has (1999+), all season types.
    // expensive; intended for the initial load or an on-demand full refresh
    void ingestFullHistory();

    // refreshes teams, players, and just the current season's stat rows, then recomputes
    // career stats and percentiles. This is what the weekly scheduled job calls
    void ingestCurrentSeason();
}
