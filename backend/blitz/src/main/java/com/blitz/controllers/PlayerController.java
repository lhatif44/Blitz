package com.blitz.controllers;

import com.blitz.model.entity.Player;
import com.blitz.service.PlayerService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController{

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService){
        this.playerService = playerService;
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
    public Player savePlayer(@RequestBody Player player){
        return playerService.savePlayer(player);
    }
}    




