package com.blitz.service;

import com.blitz.exception.ResourceNotFoundException;
import com.blitz.model.entity.Team;
import com.blitz.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//All methods are read only unless overridden
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService {

    //Repository that stores team records
    private final TeamRepository teamRepository;

    //Constructor injection for the team repository
    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    //Function to get a team by its abbreviation e.g. "KC" for Kansas City Chiefs
    //Throws a ResourceNotFoundException if no team with that abbreviation exists in the database
    public Team getTeamByAbbr(String abbr) {
        return teamRepository.findById(abbr)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with abbreviation: " + abbr));
    }

    @Override
    //Function to return every team in the database
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    //Function to return all teams in a given conference e.g. "AFC" or "NFC"
    public List<Team> getTeamsByConference(String conference) {
        return teamRepository.findByConference(conference);
    }

    @Override
    //Function to return all teams in a given division e.g. "AFC West"
    public List<Team> getTeamsByDivision(String division) {
        return teamRepository.findByDivision(division);
    }

    @Override
    //Overrides readOnly — writes a team record to the database
    //Used by the ingestion pipeline to create or update team entries
    @Transactional
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }
}
