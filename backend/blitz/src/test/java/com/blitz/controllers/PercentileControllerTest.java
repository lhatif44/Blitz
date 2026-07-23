package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.exception.ResourceNotFoundException;
import com.blitz.model.entity.CareerPercentile;
import com.blitz.service.PercentileService;
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
public class PercentileControllerTest {

    @Mock
    private PercentileService percentileService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID playerId;
    private CareerPercentile testPercentile;

    @BeforeEach
    void setUp() {
        PercentileController controller = new PercentileController(percentileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        playerId = UUID.randomUUID();
        testPercentile = new CareerPercentile();
        testPercentile.setPositionGroup("QB");
        testPercentile.setStatName("career_passing_yards");
        testPercentile.setStatValue(new BigDecimal("52000.000"));
        testPercentile.setPercentile(new BigDecimal("99.0"));
    }

    @Test
    void getPercentilesForPlayer_returns200AndList() throws Exception {
        when(percentileService.getPercentilesForPlayer(playerId)).thenReturn(List.of(testPercentile));

        mockMvc.perform(get("/api/percentiles/{playerId}", playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statName").value("career_passing_yards"))
                .andExpect(jsonPath("$[0].percentile").value(99.0));
    }

    @Test
    void getPercentilesForPlayer_returns404_whenPlayerNotFound() throws Exception {
        when(percentileService.getPercentilesForPlayer(playerId))
                .thenThrow(new ResourceNotFoundException("Player not found with ID: " + playerId));

        mockMvc.perform(get("/api/percentiles/{playerId}", playerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void computePercentilesForPlayer_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/percentiles/{playerId}/recompute", playerId))
                .andExpect(status().isOk());

        verify(percentileService).computePercentilesForPlayer(playerId);
    }

    @Test
    void computePercentilesForPositionGroup_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/percentiles/position/{positionGroup}/recompute", "QB"))
                .andExpect(status().isOk());

        verify(percentileService).computePercentilesForPositionGroup("QB");
    }

    @Test
    void computeAllPercentiles_returns200_andInvokesService() throws Exception {
        mockMvc.perform(post("/api/percentiles/recompute-all"))
                .andExpect(status().isOk());

        verify(percentileService).computeAllPercentiles();
    }
}
