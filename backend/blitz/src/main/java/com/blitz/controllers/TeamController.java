package com.blitz.controllers;

import com.blitz.model.entity.Team;
import com.blitz.service.TeamService;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/teams")
public class TeamController{
	
		
	private final TeamService teamService;

	
	public TeamController(TeamService teamService){
		this.teamService = teamService;
	}
	
	@GetMapping("/{abbr}")
    	public Team getTeamByAbbr(@PathVariable String abbr) {

        	return teamService.getTeamByAbbr(abbr);
			
    	}

	//Function that returns teams, sorted by conference or division if specified
	@GetMapping
	public List<Team> getTeams(
    		@RequestParam(required = false) String conference,
    		@RequestParam(required = false) String division
	){
    		if (division != null) {
        		return teamService.getTeamsByDivision(division);
    		}
    		
			if (conference != null) {
       	 		return teamService.getTeamsByConference(conference);
    		}
    		
			return teamService.getAllTeams();
	}

	@PostMapping
	public Team saveTeam(@RequestBody Team team){
		return teamService.saveTeam(team);
	}

}
