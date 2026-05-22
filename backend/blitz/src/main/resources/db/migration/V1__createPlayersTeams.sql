
CREATE TABLE teams (
      abbr VARCHAR(10) PRIMARY KEY,
      full_name VARCHAR(100) NOT NULL,
      conference VARCHAR(10),
      division VARCHAR(20),
      primary_color VARCHAR(7),
      logo_url TEXT
  );

  CREATE TABLE players (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nflverse_id VARCHAR(50) UNIQUE NOT NULL,
      display_name VARCHAR(100) NOT NULL,
      first_name VARCHAR(50),
      last_name VARCHAR(50),
      position VARCHAR(10),
      position_group VARCHAR(10),
      birth_date DATE,
      birth_city VARCHAR(100),
      birth_state VARCHAR(50),
      height_inches INT,
      weight_lbs INT,
      college VARCHAR(100),
      entry_year INT,
      draft_team VARCHAR(10) REFERENCES teams(abbr),
      draft_round INT,
      draft_pick INT,
      headshot_url TEXT,
      status VARCHAR(20),
      years_active VARCHAR(20),
      is_hof BOOLEAN DEFAULT FALSE,
      hof_induction_year INT
  );

  CREATE TABLE player_teams (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      player_id UUID NOT NULL REFERENCES players(id) ON DELETE CASCADE,
      team_abbr VARCHAR(10) NOT NULL REFERENCES teams(abbr),
      season_start INT NOT NULL,
      season_end INT
  );

  CREATE INDEX idx_players_position_group ON players(position_group);
  CREATE INDEX idx_players_status ON players(status);
  CREATE INDEX idx_players_display_name ON players(display_name);
  CREATE INDEX idx_player_teams_player_id ON player_teams(player_id);

