package com.blitz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

// Request payload for creating/updating a player via POST /api/players. Kept separate from the
// Player entity so the wire contract doesn't couple to persistence internals (drops playerTeams/
// achievements, which are server-managed, not client-settable) and so bad input gets a clean
// validation error instead of a raw database exception. id is optional — omit to create a new
// player, include to update an existing one, matching PlayerService's upsert-by-id semantics.
public record PlayerRequest(
        UUID id,

        @NotBlank @Size(max = 50)
        String nflverseId,

        @NotBlank @Size(max = 100)
        String displayName,

        @Size(max = 50) String firstName,
        @Size(max = 50) String lastName,

        @Size(max = 10) String position,
        @Size(max = 10) String positionGroup,

        LocalDate birthDate,
        @Size(max = 100) String birthCity,
        @Size(max = 50) String birthState,

        @Positive Integer heightInches,
        @Positive Integer weightLbs,

        @Size(max = 255) String college,

        @Min(1920) @Max(2100) Integer entryYear,

        // resolved to a Team by the controller rather than accepting a nested Team object —
        // keeps the request from being able to set arbitrary fields on someone else's team
        @Size(max = 10) String draftTeamAbbr,
        @Min(1) @Max(20) Integer draftRound,
        @Min(1) @Max(500) Integer draftPick,

        String headshotUrl,

        @Size(max = 20) String status,
        @Size(max = 20) String yearsActive,

        Boolean isHof,
        Integer hofInductionYear
) {
}
