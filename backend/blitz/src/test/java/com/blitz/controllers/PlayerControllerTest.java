package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.exception.ResourceNotFoundException;
import com.blitz.model.entity.Player;
import com.blitz.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// standalone MockMvc setup — no Spring context is loaded, so this only depends on
// spring-test + jackson + mockito, avoiding any Spring Boot test-slice auto-configuration
@ExtendWith(MockitoExtension.class)
public class PlayerControllerTest {

    @Mock
    private PlayerService playerService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Player testPlayer;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        PlayerController controller = new PlayerController(playerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        playerId = UUID.randomUUID();
        testPlayer = new Player();
        testPlayer.setId(playerId);
        testPlayer.setNflverseId("test-001");
        testPlayer.setDisplayName("Patrick Mahomes");
        testPlayer.setPositionGroup("QB");
        testPlayer.setStatus("ACT");
    }

    @Test
    void getPlayerById_returns200AndPlayer() throws Exception {
        when(playerService.getPlayerById(playerId)).thenReturn(testPlayer);

        mockMvc.perform(get("/api/players/{id}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Patrick Mahomes"))
                .andExpect(jsonPath("$.positionGroup").value("QB"));
    }

    @Test
    void getPlayerById_returns404_whenNotFound() throws Exception {
        when(playerService.getPlayerById(playerId))
                .thenThrow(new ResourceNotFoundException("Player not found with ID: " + playerId));

        mockMvc.perform(get("/api/players/{id}", playerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Player not found with ID: " + playerId));
    }

    @Test
    void getPlayerByNflverseId_returns200AndPlayer() throws Exception {
        when(playerService.getPlayerByNflverseId("test-001")).thenReturn(testPlayer);

        mockMvc.perform(get("/api/players/nflverse/{nflverseId}", "test-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nflverseId").value("test-001"));
    }

    @Test
    void getPlayers_noParams_returnsAllPlayers() throws Exception {
        when(playerService.getAllPlayers()).thenReturn(List.of(testPlayer));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(playerService).getAllPlayers();
        verify(playerService, never()).searchPlayersByName(any());
    }

    @Test
    void getPlayers_withNameParam_searchesByName() throws Exception {
        when(playerService.searchPlayersByName("mahomes")).thenReturn(List.of(testPlayer));

        mockMvc.perform(get("/api/players").param("name", "mahomes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Patrick Mahomes"));

        verify(playerService).searchPlayersByName("mahomes");
        verify(playerService, never()).getAllPlayers();
    }

    @Test
    void getPlayers_withPositionGroupParam_filtersByPositionGroup() throws Exception {
        when(playerService.getPlayersByPositionGroup("QB")).thenReturn(List.of(testPlayer));

        mockMvc.perform(get("/api/players").param("positionGroup", "QB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(playerService).getPlayersByPositionGroup("QB");
    }

    @Test
    void getPlayers_withActiveTrue_returnsActivePlayers() throws Exception {
        when(playerService.getActivePlayers()).thenReturn(List.of(testPlayer));

        mockMvc.perform(get("/api/players").param("active", "true"))
                .andExpect(status().isOk());

        verify(playerService).getActivePlayers();
    }

    @Test
    void savePlayer_returns200AndSavedPlayer() throws Exception {
        when(playerService.savePlayer(any(Player.class))).thenReturn(testPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPlayer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Patrick Mahomes"));
    }
}
