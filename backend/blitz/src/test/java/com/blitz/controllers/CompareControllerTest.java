package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.exception.InvalidComparisonException;
import com.blitz.exception.ResourceNotFoundException;
import com.blitz.model.entity.PassingStats;
import com.blitz.model.entity.Player;
import com.blitz.service.CompareService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CompareControllerTest {

    @Mock
    private CompareService compareService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID qbId1;
    private UUID qbId2;
    private Player qb1;
    private Player qb2;

    @BeforeEach
    void setUp() {
        CompareController controller = new CompareController(compareService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        qbId1 = UUID.randomUUID();
        qbId2 = UUID.randomUUID();

        qb1 = new Player();
        qb1.setDisplayName("Patrick Mahomes");
        qb1.setPositionGroup("QB");

        qb2 = new Player();
        qb2.setDisplayName("Josh Allen");
        qb2.setPositionGroup("QB");
    }

    @Test
    void comparePlayers_returns200AndComparisonResult() throws Exception {
        PassingStats stats1 = new PassingStats();
        stats1.setPassingYards(5000);
        PassingStats stats2 = new PassingStats();
        stats2.setPassingYards(4500);

        CompareService.ComparisonResult result =
                new CompareService.ComparisonResult(qb1, qb2, stats1, stats2, "QB");

        when(compareService.comparePlayers(qbId1, qbId2, "REG")).thenReturn(result);

        mockMvc.perform(get("/api/compare")
                        .param("player1", qbId1.toString())
                        .param("player2", qbId2.toString())
                        .param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player1.displayName").value("Patrick Mahomes"))
                .andExpect(jsonPath("$.player2.displayName").value("Josh Allen"))
                .andExpect(jsonPath("$.positionGroup").value("QB"));
    }

    @Test
    void comparePlayers_worksWithoutSeasonType() throws Exception {
        CompareService.ComparisonResult result =
                new CompareService.ComparisonResult(qb1, qb2, null, null, "QB");

        when(compareService.comparePlayers(eq(qbId1), eq(qbId2), any())).thenReturn(result);

        mockMvc.perform(get("/api/compare")
                        .param("player1", qbId1.toString())
                        .param("player2", qbId2.toString()))
                .andExpect(status().isOk());

        verify(compareService).comparePlayers(qbId1, qbId2, null);
    }

    @Test
    void comparePlayers_returns400_whenDifferentPositionGroups() throws Exception {
        when(compareService.comparePlayers(qbId1, qbId2, "REG"))
                .thenThrow(new InvalidComparisonException(
                        "Cannot compare players from different position groups: QB vs CB"));

        mockMvc.perform(get("/api/compare")
                        .param("player1", qbId1.toString())
                        .param("player2", qbId2.toString())
                        .param("seasonType", "REG"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void comparePlayers_returns404_whenPlayerNotFound() throws Exception {
        when(compareService.comparePlayers(qbId1, qbId2, "REG"))
                .thenThrow(new ResourceNotFoundException("Player not found with ID: " + qbId1));

        mockMvc.perform(get("/api/compare")
                        .param("player1", qbId1.toString())
                        .param("player2", qbId2.toString())
                        .param("seasonType", "REG"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void comparePlayers_returns400_whenRequiredParamMissing() throws Exception {
        mockMvc.perform(get("/api/compare").param("player1", qbId1.toString()))
                .andExpect(status().isBadRequest());
    }
}
