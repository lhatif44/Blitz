package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.model.entity.*;
import com.blitz.service.StatsService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class StatsControllerTest {

    @Mock
    private StatsService statsService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID playerId;

    @BeforeEach
    void setUp() {
        StatsController controller = new StatsController(statsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        playerId = UUID.randomUUID();
    }

    // --- Passing ---

    @Test
    void getPassingStats_returns200AndForwardsSeasonType() throws Exception {
        PassingStats stats = new PassingStats();
        stats.setPassingYards(4000);
        when(statsService.getPassingStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/passing/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passingYards").value(4000));

        verify(statsService).getPassingStats(playerId, "REG");
    }

    @Test
    void getPassingStats_withoutSeasonType_passesNull() throws Exception {
        when(statsService.getPassingStats(playerId, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/stats/passing/{playerId}", playerId))
                .andExpect(status().isOk());

        verify(statsService).getPassingStats(playerId, null);
    }

    @Test
    void savePassingStats_returns200AndSavedStats() throws Exception {
        PassingStats stats = new PassingStats();
        stats.setPassingYards(4000);
        when(statsService.savePassingStats(any(PassingStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/passing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passingYards").value(4000));
    }

    // --- Rushing ---

    @Test
    void getRushingStats_returns200AndForwardsSeasonType() throws Exception {
        RushingStats stats = new RushingStats();
        stats.setRushingYards(1200);
        when(statsService.getRushingStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/rushing/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rushingYards").value(1200));

        verify(statsService).getRushingStats(playerId, "REG");
    }

    @Test
    void saveRushingStats_returns200AndSavedStats() throws Exception {
        RushingStats stats = new RushingStats();
        stats.setRushingYards(1200);
        when(statsService.saveRushingStats(any(RushingStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/rushing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rushingYards").value(1200));
    }

    // --- Receiving ---

    @Test
    void getReceivingStats_returns200AndForwardsSeasonType() throws Exception {
        ReceivingStats stats = new ReceivingStats();
        stats.setReceivingYards(900);
        when(statsService.getReceivingStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/receiving/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receivingYards").value(900));

        verify(statsService).getReceivingStats(playerId, "REG");
    }

    @Test
    void saveReceivingStats_returns200AndSavedStats() throws Exception {
        ReceivingStats stats = new ReceivingStats();
        stats.setReceivingYards(900);
        when(statsService.saveReceivingStats(any(ReceivingStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/receiving")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivingYards").value(900));
    }

    // --- Pass Rush ---

    @Test
    void getPassRushStats_returns200AndForwardsSeasonType() throws Exception {
        PassRushStats stats = new PassRushStats();
        stats.setQbHits(20);
        when(statsService.getPassRushStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/pass-rush/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].qbHits").value(20));

        verify(statsService).getPassRushStats(playerId, "REG");
    }

    @Test
    void savePassRushStats_returns200AndSavedStats() throws Exception {
        PassRushStats stats = new PassRushStats();
        stats.setQbHits(20);
        when(statsService.savePassRushStats(any(PassRushStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/pass-rush")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qbHits").value(20));
    }

    // --- Linebacker ---

    @Test
    void getLinebackerStats_returns200AndForwardsSeasonType() throws Exception {
        LinebackerStats stats = new LinebackerStats();
        stats.setTacklesTotal(120);
        when(statsService.getLinebackerStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/linebacker/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tacklesTotal").value(120));

        verify(statsService).getLinebackerStats(playerId, "REG");
    }

    @Test
    void saveLinebackerStats_returns200AndSavedStats() throws Exception {
        LinebackerStats stats = new LinebackerStats();
        stats.setTacklesTotal(120);
        when(statsService.saveLinebackerStats(any(LinebackerStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/linebacker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tacklesTotal").value(120));
    }

    // --- Secondary ---

    @Test
    void getSecondaryStats_returns200AndForwardsSeasonType() throws Exception {
        SecondaryStats stats = new SecondaryStats();
        stats.setInterceptions(5);
        when(statsService.getSecondaryStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/secondary/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].interceptions").value(5));

        verify(statsService).getSecondaryStats(playerId, "REG");
    }

    @Test
    void saveSecondaryStats_returns200AndSavedStats() throws Exception {
        SecondaryStats stats = new SecondaryStats();
        stats.setInterceptions(5);
        when(statsService.saveSecondaryStats(any(SecondaryStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/secondary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interceptions").value(5));
    }

    // --- Kicking ---

    @Test
    void getKickingStats_returns200AndForwardsSeasonType() throws Exception {
        KickingStats stats = new KickingStats();
        stats.setFgMade(30);
        when(statsService.getKickingStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/kicking/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fgMade").value(30));

        verify(statsService).getKickingStats(playerId, "REG");
    }

    @Test
    void saveKickingStats_returns200AndSavedStats() throws Exception {
        KickingStats stats = new KickingStats();
        stats.setFgMade(30);
        when(statsService.saveKickingStats(any(KickingStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/kicking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fgMade").value(30));
    }

    // --- Punting ---

    @Test
    void getPuntingStats_returns200AndForwardsSeasonType() throws Exception {
        PuntingStats stats = new PuntingStats();
        stats.setPunts(60);
        when(statsService.getPuntingStats(playerId, "REG")).thenReturn(List.of(stats));

        mockMvc.perform(get("/api/stats/punting/{playerId}", playerId).param("seasonType", "REG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].punts").value(60));

        verify(statsService).getPuntingStats(playerId, "REG");
    }

    @Test
    void savePuntingStats_returns200AndSavedStats() throws Exception {
        PuntingStats stats = new PuntingStats();
        stats.setPunts(60);
        when(statsService.savePuntingStats(any(PuntingStats.class))).thenReturn(stats);

        mockMvc.perform(post("/api/stats/punting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stats)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.punts").value(60));
    }
}
