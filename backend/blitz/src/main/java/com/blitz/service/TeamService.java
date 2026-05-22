package com.blitz.service;

import com.blitz.model.entity.Team;

import java.util.List;

public interface TeamService {

    // get a single team by its abbreviation e.g. "NE", "KC"
    Team getTeamByAbbr(String abbr);

    // get all teams
    List<Team> getAllTeams();

    // get all teams in a conference e.g. "AFC" or "NFC"
    List<Team> getTeamsByConference(String conference);

    // get all teams in a division e.g. "AFC East"
    List<Team> getTeamsByDivision(String division);

    // save or update a team — used by the ingestion pipeline
    Team saveTeam(Team team);
}
