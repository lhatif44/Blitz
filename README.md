# Blitz

Blitz is an NFL player analytics web app. It surfaces player bios, per-season stats, career totals, head-to-head comparisons, and historical percentile rankings.

## Features

- **Player search & profiles** — bio info plus year-by-year stats for any position
- **Career totals** — aggregated career stats computed from every regular season on record
- **Percentile rankings** — where a player's career numbers fall relative to every qualifying player at the same position group
- **Head-to-head comparisons** — compare two players' stats side by side (same position group only)

## Tech Stack

- Java 21 / Spring Boot
- PostgreSQL
- JPA / Hibernate, Flyway migrations
- Maven

## Getting Started

**Prerequisites:** Java 21, Maven, Docker

1. Start the database:
   ```bash
   cd backend/blitz
   docker compose up -d
   ```
   This spins up a local PostgreSQL instance using the credentials in `docker-compose.yml` (local development only — override via environment variables for any non-local environment).

2. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Flyway applies all database migrations automatically on startup. The API listens on `http://localhost:8080`.

3. Run the tests:
   ```bash
   ./mvnw test
   ```

## Project Layout

```
backend/blitz/    Spring Boot API — entities, repositories, services, migrations
```

## Status

The data model, service layer, REST controllers, and nflverse ingestion pipeline (weekly scheduled job + on-demand endpoints) are built and tested. Still to come: the React frontend.

For database schema, service internals, and data flow details, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
