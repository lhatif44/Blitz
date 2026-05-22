package com.blitz.service;

import com.blitz.model.entity.Player;
import com.blitz.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// service tests use Mockito to fake the repository — no real database needed
@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository; // fake repository — we control what it returns

    @InjectMocks
    private PlayerServiceImpl playerService; // the real service, injected with the fake repository

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        // create a reusable test player before each test
        testPlayer = new Player();
        testPlayer.setNflverseId("test-001");
        testPlayer.setDisplayName("Patrick Mahomes");
        testPlayer.setPositionGroup("QB");
        testPlayer.setStatus("ACT");
    }

    @Test
    void getPlayerById_returnsPlayer_whenFound() {
        UUID id = UUID.randomUUID();
        testPlayer.setId(id);

        // tell the fake repository what to return when findById is called
        when(playerRepository.findById(id)).thenReturn(Optional.of(testPlayer));

        Player result = playerService.getPlayerById(id);

        assertThat(result.getDisplayName()).isEqualTo("Patrick Mahomes");
        verify(playerRepository, times(1)).findById(id); // confirm findById was called once
    }

    @Test
    void getPlayerById_throwsException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(playerRepository.findById(id)).thenReturn(Optional.empty());

        // confirm a RuntimeException is thrown with the right message
        assertThatThrownBy(() -> playerService.getPlayerById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    void getPlayerByNflverseId_returnsPlayer_whenFound() {
        when(playerRepository.findByNflverseId("test-001")).thenReturn(Optional.of(testPlayer));

        Player result = playerService.getPlayerByNflverseId("test-001");

        assertThat(result.getNflverseId()).isEqualTo("test-001");
    }

    @Test
    void searchPlayersByName_returnsMatchingPlayers() {
        when(playerRepository.findByDisplayNameContainingIgnoreCase("mahomes"))
                .thenReturn(List.of(testPlayer));

        List<Player> results = playerService.searchPlayersByName("mahomes");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDisplayName()).isEqualTo("Patrick Mahomes");
    }

    @Test
    void getActivePlayers_returnsOnlyActivePlayers() {
        when(playerRepository.findByStatus("ACT")).thenReturn(List.of(testPlayer));

        List<Player> results = playerService.getActivePlayers();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo("ACT");
    }

    @Test
    void savePlayer_savesAndReturnsPlayer() {
        when(playerRepository.save(testPlayer)).thenReturn(testPlayer);

        Player result = playerService.savePlayer(testPlayer);

        assertThat(result.getDisplayName()).isEqualTo("Patrick Mahomes");
        verify(playerRepository, times(1)).save(testPlayer);
    }
}
