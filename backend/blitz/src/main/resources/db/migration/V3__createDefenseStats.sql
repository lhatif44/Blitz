CREATE TABLE pass_rush_stats (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) REFERENCES teams(abbr),
      season INT NOT NULL,
      season_type VARCHAR(10) NOT NULL DEFAULT 'REG',
      games INT,
      snaps INT,
      tackles_solo INT,
      tackles_assist INT,
      tackles_total INT,
      sacks DECIMAL(5,1),
      tfl DECIMAL(5,1),
      qb_hits INT,
      qb_hurries INT,
      pressures INT,
      pass_rush_win_rate DECIMAL(5,2),
      pressure_rate DECIMAL(5,2),
      sack_pct DECIMAL(5,2),
      run_stops INT,
      run_stop_pct DECIMAL(5,2),
      stuffs INT,
      forced_fumbles INT,
      fumble_recoveries INT,
      fumble_return_tds INT,
      UNIQUE(player_id, season, season_type)
  );

  CREATE TABLE linebacker_stats (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) REFERENCES teams(abbr),
      season INT NOT NULL,
      season_type VARCHAR(10) NOT NULL DEFAULT 'REG',
      games INT,
      snaps INT,
      tackles_solo INT,
      tackles_assist INT,
      tackles_total INT,
      sacks DECIMAL(5,1),
      tfl DECIMAL(5,1),
      qb_hits INT,
      targets_allowed INT,
      receptions_allowed INT,
      yards_allowed INT,
      tds_allowed INT,
      passer_rating_allowed DECIMAL(6,2),
      completion_pct_allowed DECIMAL(5,2),
      interceptions INT,
      int_yards INT,
      int_tds INT,
      passes_defended INT,
      forced_fumbles INT,
      fumble_recoveries INT,
      UNIQUE(player_id, season, season_type)
  );

  CREATE TABLE secondary_stats (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) REFERENCES teams(abbr),
      season INT NOT NULL,
      season_type VARCHAR(10) NOT NULL DEFAULT 'REG',
      games INT,
      snaps INT,
      snaps_in_coverage INT,
      tackles_solo INT,
      tackles_assist INT,
      tackles_total INT,
      targets_allowed INT,
      receptions_allowed INT,
      yards_allowed INT,
      tds_allowed INT,
      passer_rating_allowed DECIMAL(6,2),
      completion_pct_allowed DECIMAL(5,2),
      yards_per_target_allowed DECIMAL(5,2),
      burned_rate DECIMAL(5,2),
      interceptions INT,
      int_yards INT,
      int_return_tds INT,
      passes_defended INT,
      forced_fumbles INT,
      run_stops INT,
      run_stop_pct DECIMAL(5,2),
      UNIQUE(player_id, season, season_type)
  );

  CREATE INDEX idx_pass_rush_stats_player_id ON pass_rush_stats(player_id);
  CREATE INDEX idx_pass_rush_stats_season ON pass_rush_stats(season);
  CREATE INDEX idx_linebacker_stats_player_id ON linebacker_stats(player_id);
  CREATE INDEX idx_linebacker_stats_season ON linebacker_stats(season);
  CREATE INDEX idx_secondary_stats_player_id ON secondary_stats(player_id);
  CREATE INDEX idx_secondary_stats_season ON secondary_stats(season);

