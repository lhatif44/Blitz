-- career_stats stores one row per player per career-level stat
-- e.g. player=Mahomes, stat_name="career_passing_yards", stat_value=52000
-- This table is recomputed by CareerStatsService whenever new season data is ingested
-- It feeds both the player profile page (career totals display) and the percentile ranking engine

CREATE TABLE career_stats (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id       UUID            NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    position_group  VARCHAR(10)     NOT NULL,
    stat_name       VARCHAR(100)    NOT NULL,
    stat_value      NUMERIC(12, 3)  NOT NULL,
    computed_at     TIMESTAMP       NOT NULL,

    -- Enforce one career value per stat per player — recompute replaces the old value
    CONSTRAINT uq_career_stats_player_stat UNIQUE (player_id, stat_name)
);

-- Used by CareerStatsService to fetch all players at a position group for ranking
CREATE INDEX idx_career_stats_player_id       ON career_stats(player_id);
CREATE INDEX idx_career_stats_position_group  ON career_stats(position_group);
