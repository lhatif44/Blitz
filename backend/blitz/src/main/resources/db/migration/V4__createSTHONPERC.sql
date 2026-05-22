CREATE TABLE kicking_stats (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) REFERENCES teams(abbr),
      season INT NOT NULL,
      season_type VARCHAR(10) NOT NULL DEFAULT 'REG',
      games INT,
      fg_made INT,
      fg_attempted INT,
      fg_pct DECIMAL(5,2),
      fg_1_19_made INT,
      fg_1_19_attempted INT,
      fg_20_29_made INT,
      fg_20_29_attempted INT,
      fg_30_39_made INT,
      fg_30_39_attempted INT,
      fg_40_49_made INT,
      fg_40_49_attempted INT,
      fg_50_plus_made INT,
      fg_50_plus_attempted INT,
      fg_long INT,
      xp_made INT,
      xp_attempted INT,
      xp_pct DECIMAL(5,2),
      kickoffs INT,
      touchbacks INT,
      touchback_pct DECIMAL(5,2),
      UNIQUE(player_id, season, season_type)
  );

  CREATE TABLE punting_stats (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) REFERENCES teams(abbr),
      season INT NOT NULL,
      season_type VARCHAR(10) NOT NULL DEFAULT 'REG',
      games INT,
      punts INT,
      punt_yards INT,
      gross_avg DECIMAL(5,2),
      net_avg DECIMAL(5,2),
      punts_inside_20 INT,
      touchbacks INT,
      long_punt INT,
      fair_catches_forced INT,
      UNIQUE(player_id, season, season_type)
  );

  CREATE TABLE achievements (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      type VARCHAR(30) NOT NULL,
      season INT,
      detail VARCHAR(100)
  );

  CREATE TABLE career_percentiles (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      position_group VARCHAR(10) NOT NULL,
      stat_name VARCHAR(50) NOT NULL,
      stat_value DECIMAL(12,3),
      percentile DECIMAL(5,2),
      computed_at TIMESTAMP NOT NULL DEFAULT now(),
      UNIQUE(player_id, stat_name)
  );

  CREATE INDEX idx_kicking_stats_player_id ON kicking_stats(player_id);
  CREATE INDEX idx_punting_stats_player_id ON punting_stats(player_id);
  CREATE INDEX idx_achievements_player_id ON achievements(player_id);
  CREATE INDEX idx_achievements_type ON achievements(type);
  CREATE INDEX idx_career_percentiles_player_id ON
  career_percentiles(player_id);
  CREATE INDEX idx_career_percentiles_position_group ON
  career_percentiles(position_group);
  
