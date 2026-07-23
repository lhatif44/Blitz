package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.exception.ResourceNotFoundException;
import com.blitz.model.entity.CareerStats;
import com.blitz.service.CareerStatsService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CareerStatsControllerTest {

    @Mock
    private CareerStatsService careerStatsService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID playerId;
    private CareerStats testStat;

    @BeforeEach
    void setUp() {
        CareerStatsController controller = new CareerStatsController(careerStatsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        playerId = UUID.randomUUID();
        testStat = new CareerStats();
        testStat.setPositionGroup("QB");
        testStat.setStatName("career_passing_yards");
        testStat.setStatValue(new BigDecimal("52000.000"));
    }

    @Test
    void getCareerStatsForPlayer_returns200AndList() throws Exception {
        when(careerStatsService.getCareerStatsForPlayer(playerId)).thenReturn(List.of(testStat));

        mockMvc.perform(get("/api/career-stats/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statName").value("career_passing_yards"))
                .andExpect(jsonPath("$[0].statValue").value(52000.000));
    }

    @Test
    void getCareerStatsForPlayer_returns404_whenPlayerNotFound() throws Exception {
        when(careerStatsService.getCareerStatsForPlayer(playerId))
                .thenThrow(new ResourceNotFoundException("Player not found with ID: " + playerId));

        mockMvc.perform(get("/api/career-stats/{playerId}", playerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void computeCareerStatsForPlayer_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/career-stats/{playerId}/recompute", playerId))
                .andExpect(status().isOk());

        verify(careerStatsService).computeCareerStatsForPlayer(playerId);
    }

    @Test
    void computeCareerStatsForPositionGroup_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/career-stats/position/{positionGroup}/recompute", "QB"))
                .andExpect(status().isOk());

        verify(careerStatsService).computeCareerStatsForPositionGroup("QB");
    }

    @Test
    void computeAllCareerStats_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/career-stats/recompute-all"))
                .andExpect(status().isOk());

        verify(careerStatsService).computeAllCareerStats();
    }
}
