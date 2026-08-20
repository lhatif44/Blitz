package com.blitz.controllers;

import com.blitz.dto.PlayerRequest;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import com.blitz.service.PlayerService;
import com.blitz.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController{

    private final PlayerService playerService;
    private final TeamService teamService;

    public PlayerController(PlayerService playerService, TeamService teamService){
        this.playerService = playerService;
        this.teamService = teamService;
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable UUID id) {

        return playerService.getPlayerById(id);

    }

    @GetMapping("/nflverse/{nflverseId}")
    public Player getPlayerByNflverseId(@PathVariable String nflverseId) {

        return playerService.getPlayerByNflverseId(nflverseId);
    }

    @GetMapping
    public List<Player> getPlayers(
        @RequestParam(required=false) String name,
        @RequestParam(required=false) String positionGroup,
        @RequestParam(required=false) Boolean active
    ){
        if(name != null){
            return playerService.searchPlayersByName(name);
        }
        if (positionGroup != null){
            return playerService.getPlayersByPositionGroup(positionGroup);
        }
        if (Boolean.TRUE.equals(active)){
            return playerService.getActivePlayers();
        }
        return playerService.getAllPlayers();

    }

    @PostMapping
    public Player savePlayer(@Valid @RequestBody PlayerRequest request){
        Player player = new Player();
        player.setId(request.id());
        player.setNflverseId(request.nflverseId());
        player.setDisplayName(request.displayName());
        player.setFirstName(request.firstName());
        player.setLastName(request.lastName());
        player.setPosition(request.position());
        player.setPositionGroup(request.positionGroup());
        player.setBirthDate(request.birthDate());
        player.setBirthCity(request.birthCity());
        player.setBirthState(request.birthState());
        player.setHeightInches(request.heightInches());
        player.setWeightLbs(request.weightLbs());
        player.setCollege(request.college());
        player.setEntryYear(request.entryYear());
        if (request.draftTeamAbbr() != null) {
            Team draftTeam = teamService.getTeamByAbbr(request.draftTeamAbbr());
            player.setDraftTeam(draftTeam);
        }
        player.setDraftRound(request.draftRound());
        player.setDraftPick(request.draftPick());
        player.setHeadshotUrl(request.headshotUrl());
        player.setStatus(request.status());
        player.setYearsActive(request.yearsActive());
        player.setIsHof(request.isHof());
        player.setHofInductionYear(request.hofInductionYear());
        return playerService.savePlayer(player);
    }
}
