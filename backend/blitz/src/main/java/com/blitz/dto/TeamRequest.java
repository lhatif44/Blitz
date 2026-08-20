package com.blitz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Request payload for creating/updating a team via POST /api/teams. abbr is the primary key
// (not auto-generated) and doubles as the identifier for upserting an existing team, matching
// TeamService's upsert-by-abbr semantics.
public record TeamRequest(
        @NotBlank @Size(max = 10)
        String abbr,

        @NotBlank @Size(max = 100)
        String fullName,

        @Size(max = 10) String conference,
        @Size(max = 20) String division,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "must be a hex color like #E31837")
        String primaryColor,

        String logoUrl
) {
}
