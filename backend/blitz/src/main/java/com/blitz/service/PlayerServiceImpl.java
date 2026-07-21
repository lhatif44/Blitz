package com.blitz.service;

  import com.blitz.model.entity.Player;
  import com.blitz.repository.PlayerRepository;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  import com.blitz.exception.ResourceNotFoundException;

  import java.util.List;
  import java.util.UUID;

@Service
//All methods are read only unless overridden
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService{

    //repository that stores players
    private final PlayerRepository playerRepository;
    

    //Constructor for repository
    public PlayerServiceImpl(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    @Override
    //Function that takes in a UUID id number and uses it to return a player
    //Throws Runtime exception if not found
    public Player getPlayerById(UUID id){
        return playerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
    }

    @Override
    //Function to get a player by NFLverseId
    //Throws Runtime exception if not found
    public Player getPlayerByNflverseId(String nflverseId){
        return playerRepository.findByNflverseId(nflverseId).orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + nflverseId));
    }

    @Override 
    //Function to get a player by name
    //not case sensitive
    public List<Player> searchPlayersByName(String name){
        return playerRepository.findByDisplayNameContainingIgnoreCase(name);
    }

    @Override
    //Function to get a player by their position group
    public List<Player> getPlayersByPositionGroup(String positionGroup){
        return playerRepository.findByPositionGroup(positionGroup);
    }

    @Override
    //Function to get active players
    public List<Player> getActivePlayers(){
        return playerRepository.findByStatus("ACT");
    }

    @Override
    //Function to return all players
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    // overrides readOnly = true since this method writes to the database
    @Transactional
    //Function to save a player to repository
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }
}
