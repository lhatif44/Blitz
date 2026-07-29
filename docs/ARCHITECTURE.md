# Blitz — Backend Architecture

Technical reference for the Spring Boot backend: schema, layers, and data flow. For setup and running the app, see the [root README](../README.md).

---

## Project Structure

```
src/main/java/com/blitz/
  BlitzApplication.java          Entry point — enables scheduling for the ingestion job
  model/entity/                  JPA entities — one class per database table
  repository/                    Spring Data repositories — one interface per entity
  service/                       Business logic — interfaces + implementations
  controllers/                   Rest Controllers

src/main/resources/
  application.properties         Database connection, JPA, Flyway config
  db/migration/                  Flyway SQL migration scripts (V1 through V5)

src/test/java/com/blitz/
  repository/                    Integration tests (@DataJpaTest)
  service/                       Unit tests (Mockito)
  controllers                    HTTP tests
```

---

## Database

PostgreSQL 16 running in Docker. Flyway runs all migration scripts automatically on startup in version order.

`spring.jpa.hibernate.ddl-auto=validate` means Hibernate checks that the entity definitions match the database schema on startup but never modifies the schema itself. All schema changes go through Flyway migrations only. Connection settings live in `application.properties` and default to the local Docker Postgres instance defined in `docker-compose.yml` — see the README for local setup.

---

## Migrations

| File | Tables Created |
|------|---------------|
| V1__createPlayersTeams.sql | players, teams, player_teams |
| V2__createOffenseStats.sql | passing_stats, rushing_stats, receiving_stats |
| V3__createDefenseStats.sql | pass_rush_stats, linebacker_stats, secondary_stats |
| V4__createSTHONPERC.sql | kicking_stats, punting_stats, achievements, career_percentiles |
| V5__createCareerStats.sql | career_stats |

---

## Database Schema

### players
Stores one row per NFL player (active and historical).

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | Primary key |
| nflverse_id | VARCHAR | Unique identifier from nflverse data source |
| display_name | VARCHAR | Full name |
| first_name, last_name | VARCHAR | |
| position | VARCHAR | Specific position (e.g. "MLB") |
| position_group | VARCHAR | Grouped position (e.g. "LB") — used for all comparisons and percentiles |
| birth_date | DATE | |
| birth_city, birth_state | VARCHAR | |
| height_inches, weight_lbs | INT | Physical measurements |
| college | VARCHAR | |
| entry_year | INT | Draft/entry year |
| draft_team | VARCHAR (FK → teams) | |
| draft_round, draft_pick | INT | |
| headshot_url | VARCHAR | Profile image URL |
| status | VARCHAR | "ACT" for active, "RET" for retired, etc. |
| years_active | VARCHAR | e.g. "2018-2024" |
| is_hof | BOOLEAN | Hall of Fame status |
| hof_induction_year | INT | |

### teams
One row per NFL franchise.

| Column | Type | Notes |
|--------|------|-------|
| abbr | VARCHAR (PK) | e.g. "KC", "NE" |
| full_name | VARCHAR | e.g. "Kansas City Chiefs" |
| conference | VARCHAR | "AFC" or "NFC" |
| division | VARCHAR | e.g. "AFC West" |
| primary_color | VARCHAR | Hex color code, used for UI theming |
| logo_url | VARCHAR | |

### player_teams
Join table linking players to teams with years active.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID (PK) | |
| player_id | UUID (FK → players) | |
| team_abbr | VARCHAR (FK → teams) | |
| season_start | INT | |
| season_end | INT | NULL if still on team |

### Per-Season Stats Tables
Each table stores one row per player per season per season type (REG or POST).

All stat tables share this base structure:

| Column | Type |
|--------|------|
| id | UUID (PK) |
| player_id | UUID (FK → players) |
| season | INT | e.g. 2023 |
| season_type | VARCHAR | "REG" or "POST" |
| games | INT | |
| ... stat columns ... | | Position-specific |

**Stat tables and their position groups:**

| Table | Position Groups |
|-------|----------------|
| passing_stats | QB |
| rushing_stats | RB |
| receiving_stats | WR, TE |
| pass_rush_stats | DE, DT, EDGE |
| linebacker_stats | LB |
| secondary_stats | CB, S |
| kicking_stats | K |
| punting_stats | P |

### career_stats
Stores one aggregated career-level stat per player per stat name. Computed by CareerStatsService from the per-season tables. Powers both the player profile page (career totals display) and the percentile ranking engine.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID (PK) | |
| player_id | UUID (FK → players) | |
| position_group | VARCHAR | Stored directly to avoid joins |
| stat_name | VARCHAR | e.g. "career_passing_yards" |
| stat_value | NUMERIC(12,3) | Computed career total |
| computed_at | TIMESTAMP | Last recompute time |

Unique constraint on (player_id, stat_name) — one value per stat per player.

**Example rows for Patrick Mahomes:**
```
career_passing_yards    → 52000
career_passing_tds      → 400
career_completion_pct   → 67.20
career_yards_per_attempt → 8.41
career_epa_per_dropback → 0.312
```

### career_percentiles
Stores each player's percentile rank (0–100) for each stat, relative to all qualifying historical players at the same position group.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID (PK) | |
| player_id | UUID (FK → players) | |
| position_group | VARCHAR | |
| stat_name | VARCHAR | Same stat names as career_stats |
| stat_value | NUMERIC | Copied from career_stats |
| percentile | NUMERIC(4,1) | 0.0 to 100.0 |
| computed_at | TIMESTAMP | |

### achievements
Stores notable career achievements and awards per player (Pro Bowls, All-Pro selections, etc.)

---

## Entity Layer

One Java class per table, located in `model/entity/`. All entities use:
- `@Entity` + `@Table(name = "...")` — JPA mapping
- `UUID` primary key with `@GeneratedValue(strategy = GenerationType.UUID)`
- `@ManyToOne(fetch = FetchType.LAZY)` for foreign keys
- `@Getter` + `@Setter` from Lombok — eliminates boilerplate

---

## Repository Layer

One interface per entity, located in `repository/`. All extend `JpaRepository<Entity, UUID>` — Spring Data generates the SQL from method names automatically.

**PlayerRepository:**
```java
findByNflverseId(String nflverseId)
findByDisplayNameContainingIgnoreCase(String name)   // powers search bar
findByPositionGroup(String positionGroup)
findByStatus(String status)
```

**All 8 stat repositories share this pattern:**
```java
findByPlayerId(UUID playerId)                                        // all seasons
findByPlayerIdAndSeasonType(UUID, String)                            // REG or POST only
findByPlayerIdAndSeasonAndSeasonType(UUID, Integer, String)          // specific year
```

**CareerStatsRepository / CareerPercentileRepository:**
```java
findByPlayerId(UUID playerId)
findByPositionGroup(String positionGroup)
deleteByPlayerId(UUID playerId)
deleteByPositionGroup(String positionGroup)
```

---

## Service Layer

All services follow the same pattern:
- An **interface** defines the contract
- An **Impl** class annotated `@Service` provides the implementation
- Class-level `@Transactional(readOnly = true)` — all reads are non-blocking by default
- Write methods override with `@Transactional` individually
- Constructor injection — no `@Autowired`

### PlayerService
Handles player lookup and search.

```
getPlayerById(UUID)               → single player by internal UUID
getPlayerByNflverseId(String)     → single player by nflverse ID
searchPlayersByName(String)       → case-insensitive name search (powers search bar)
getPlayersByPositionGroup(String) → all players at a position group
getActivePlayers()                → all current NFL players
getAllPlayers()                   → active + retired
savePlayer(Player)                → create/update (used by ingestion pipeline)
```

### TeamService
Handles team lookup.

```
getTeamByAbbr(String)           → single team by abbreviation e.g. "KC"
getAllTeams()                    → all 32 teams
getTeamsByConference(String)    → e.g. all "AFC" teams
getTeamsByDivision(String)      → e.g. all "AFC West" teams
saveTeam(Team)                  → create/update (used by ingestion pipeline)
```

### StatsService
Fetches per-season stat rows for any position group.

```
getPassingStats(UUID, String seasonType)      → QB stats
getRushingStats(UUID, String seasonType)      → RB stats
getReceivingStats(UUID, String seasonType)    → WR/TE stats
getPassRushStats(UUID, String seasonType)     → DE/DT/EDGE stats
getLinebackerStats(UUID, String seasonType)   → LB stats
getSecondaryStats(UUID, String seasonType)    → CB/S stats
getKickingStats(UUID, String seasonType)      → K stats
getPuntingStats(UUID, String seasonType)      → P stats
save*Stats(...)                               → 8 save methods, one per stat type
```

`seasonType` routing:
- Pass `"REG"` → regular season rows only
- Pass `"POST"` → playoff rows only
- Pass `null` → all rows across both season types

### CompareService
Head-to-head player comparison. Enforces that both players must be in the same position group — comparing a QB to a CB is blocked at the service level and would also be blocked on the frontend.

```
validateSamePositionGroup(UUID, UUID)           → throws if positions differ
comparePlayers(UUID, UUID, String seasonType)   → returns ComparisonResult
```

`ComparisonResult` is an inner class containing:
- Both `Player` objects
- Both players' stats (typed as `Object` since each position uses a different stats class)
- The shared `positionGroup` string

The switch inside `comparePlayers` routes to the correct stats table based on position group:
```
QB         → passing_stats
RB         → rushing_stats
WR, TE     → receiving_stats
DE, DT, EDGE → pass_rush_stats
LB         → linebacker_stats
CB, S      → secondary_stats
K          → kicking_stats
P          → punting_stats
```

### CareerStatsService
Aggregates per-season rows into career totals and saves them to `career_stats`. This is the only service that reads raw season data and writes derived totals.

```
getCareerStatsForPlayer(UUID)                 → read stored career totals
computeCareerStatsForPlayer(UUID)             → recompute one player's position group
computeCareerStatsForPositionGroup(String)    → recompute all players at a position
computeAllCareerStats()                       → full refresh across all positions
```

**How it computes:**
- Filters to `REG` season rows only — playoffs don't count
- Applies minimum thresholds before qualifying a player:

| Position | Threshold |
|----------|-----------|
| QB | 500 pass attempts |
| RB | 200 carries |
| WR, TE | 100 targets |
| DE, DT, EDGE, LB, CB, S | 32 games (2 seasons) |
| K, P | 2 seasons |

- Volume stats (yards, TDs, tackles) are summed across all seasons
- Rate stats (completion %, yards per attempt, pass rush win rate, etc.) use **weighted averages** — seasons with higher volume (more attempts, more snaps, more targets) carry more weight. This prevents a low-volume season from distorting a career rate unfairly.

**Example — QB EPA per dropback:**
```
Season 1: EPA 0.10, 200 attempts
Season 2: EPA 0.30, 400 attempts
Career EPA = (0.10 × 200 + 0.30 × 400) / 600 = 0.233
```

### PercentileService
Reads pre-computed career totals from `career_stats` and ranks each player within their position group using PERCENT_RANK logic.

```
getPercentilesForPlayer(UUID)                 → read stored percentiles
computePercentilesForPlayer(UUID)             → recompute one player's position group
computePercentilesForPositionGroup(String)    → recompute all players at a position
computeAllPercentiles()                       → full refresh across all positions
```

**How it ranks:**
```
percentile = (number of players with a strictly lower value) / (total players - 1) × 100
```

Examples:
- 1 of 10 players → 0th percentile
- 5 of 10 players → ~55th percentile
- 9 of 10 players → 100th percentile
- Only 1 player qualifies → always 100th percentile
- Tied players → both receive the same lower percentile (PERCENT_RANK behavior)

### IngestionService
Pulls CSV releases from `nflverse-data` on GitHub and loads them through the existing service layer. Deliberately not `@Transactional` at this level — each underlying save commits in its own transaction, so a full-history run that fails partway through keeps whatever it already ingested instead of rolling back everything.

```
ingestFullHistory()       → every season since 1999, all season types — expensive, for the initial load
ingestCurrentSeason()     → refreshes teams, players, and the current (+ next, if started) season only
```

Both methods ingest teams → players → advanced PFR stats (2018+ only) → per-season stat rows, then call `CareerStatsService.computeAllCareerStats()` and `PercentileService.computeAllPercentiles()` to refresh derived data. Re-running ingestion upserts in place (matched by player/season/season-type) instead of inserting duplicates. Historical team abbreviations (`SD`, `STL`, `OAK`) are normalized to the relocated franchise's current abbreviation before lookup.

`NflverseClient` does the actual HTTP + CSV parsing; `PositionGroupRouter` decides which stat table a row's position belongs to; one `*Mapper` per stat table converts a CSV row into an entity.

---

## Controller Layer

One `@RestController` per resource, located in `controllers/`, all under `/api`. Controllers are thin — they parse request params and delegate directly to the matching service.

| Controller | Base Path | Notes |
|------------|-----------|-------|
| PlayerController | /api/players | `GET` supports `name`, `positionGroup`, `active` query params (name search takes priority) |
| TeamController | /api/teams | `GET` supports `conference`, `division` query params |
| StatsController | /api/stats | One GET + POST pair per position group (`/passing`, `/rushing`, ... `/punting`), each GET takes an optional `seasonType` |
| CompareController | /api/compare | `GET ?player1={id}&player2={id}&seasonType=` → `CompareService.comparePlayers()` |
| CareerStatsController | /api/career-stats | Read + on-demand recompute (single player / position group / all) |
| PercentileController | /api/percentiles | Read + on-demand recompute (single player / position group / all) |
| IngestionController | /api/ingestion | `POST /full`, `POST /current` — manual trigger for either ingestion mode |

---

## Data Flow

### Ingestion (weekly scheduled job)
```
IngestionScheduler (@Scheduled, every Tuesday 06:00) or POST /api/ingestion/{full,current}
  └─→ IngestionService.ingestFullHistory() / ingestCurrentSeason()
        └─→ NflverseClient.download()          fetch + parse CSV from GitHub
        └─→ *Mapper.map()                      CSV row → entity
        └─→ StatsService.save*()               insert/update per-season rows (upsert by player/season/type)
        └─→ CareerStatsService.computeAllCareerStats()    recompute from raw rows
        └─→ PercentileService.computeAllPercentiles()     rerank from career totals
```

### Player Profile Page request
```
GET /api/players/{id}
  └─→ PlayerService.getPlayerById()          bio info
GET /api/stats/{position}/{id}
  └─→ StatsService.get*(id, null)            all season rows (year-by-year table)
GET /api/career-stats/{id}
  └─→ CareerStatsService.getCareerStatsForPlayer(id)  career totals display
GET /api/percentiles/{id}
  └─→ PercentileService.getPercentilesForPlayer(id)   percentile bars
```

### Compare Page request
```
GET /api/compare?player1={id}&player2={id}&seasonType=REG
  └─→ CompareController → CompareService.comparePlayers()
        ├─→ PlayerService (fetch both players)
        ├─→ validateSamePositionGroup() (throws if mismatch)
        └─→ StatsService.get*(id, "REG") (fetch both players' stats)
```

---

## Test Coverage

| Test Class | Type | What It Covers |
|------------|------|---------------|
| PlayerRepositoryTest | Integration (@DataJpaTest) | Real DB queries against H2 |
| PlayerServiceTest | Unit (Mockito) | All 7 service methods |
| TeamServiceTest | Unit (Mockito) | All 5 service methods |
| StatsServiceTest | Unit (Mockito) | REG/POST routing, null fallback (17 tests) |
| CompareServiceTest | Unit (Mockito) | Position group enforcement, stat routing |
| CareerStatsServiceTest | Unit (Mockito) | Every formula for every position (28 tests) |
| PlayerControllerTest, TeamControllerTest, StatsControllerTest, CompareControllerTest, CareerStatsControllerTest, PercentileControllerTest | HTTP (@WebMvcTest) | Request routing/params → correct service call |
| PositionGroupRouterTest, PassingStatsMapperTest, KickingStatsMapperTest | Unit | CSV row → entity mapping, position routing |
| IngestionServiceImplTest | Unit (Mockito) | Full-history vs. current-season orchestration |
| PercentileServiceTest | Unit (Mockito) | Rank formula: 2/3/4-player, ties, single player (12 tests) |
