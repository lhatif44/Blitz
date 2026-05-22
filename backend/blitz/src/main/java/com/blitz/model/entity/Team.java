package com.blitz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    // primary key is the team abbreviation e.g. "NE", "KC"
    @Id
    @Column(name = "abbr", length = 10)
    private String abbr;

    @Column(name = "full_name")
    private String fullName;

    // "AFC" or "NFC"
    private String conference;

    // "AFC East", "NFC West", etc.
    private String division;

    // hex color code used for UI theming
    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "logo_url")
    private String logoUrl;

    // one team drafted many players — no cascade, deleting a team should not delete players
    @OneToMany(mappedBy = "draftTeam", fetch = FetchType.LAZY)
    private List<Player> draftedPlayers;
}
