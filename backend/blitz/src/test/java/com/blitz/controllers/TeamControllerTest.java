package com.blitz.controllers;

import com.blitz.exception.GlobalExceptionHandler;
import com.blitz.model.entity.Team;
import com.blitz.service.TeamService;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TeamControllerTest {

    @Mock
    private TeamService teamService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Team testTeam;

    @BeforeEach
    void setUp() {
        TeamController controller = new TeamController(teamService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testTeam = new Team();
        testTeam.setAbbr("KC");
        testTeam.setFullName("Kansas City Chiefs");
        testTeam.setConference("AFC");
        testTeam.setDivision("AFC West");
    }

    @Test
    void getTeamByAbbr_returns200AndTeam() throws Exception {
        when(teamService.getTeamByAbbr("KC")).thenReturn(testTeam);

        mockMvc.perform(get("/api/teams/{abbr}", "KC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Kansas City Chiefs"));
    }

    @Test
    void getTeamByAbbr_throwsException_whenNotFound() {
        // TeamServiceImpl throws a plain RuntimeException, which isn't mapped by
        // GlobalExceptionHandler — with standalone MockMvc (no servlet container
        // error-page handling) it propagates out of perform() rather than becoming
        // a 500 response
        when(teamService.getTeamByAbbr("XX")).thenThrow(new RuntimeException("Team not found with abbreviation: XX"));

        assertThatThrownBy(() -> mockMvc.perform(get("/api/teams/{abbr}", "XX")))
                .hasRootCauseMessage("Team not found with abbreviation: XX");
    }

    @Test
    void getTeams_noParams_returnsAllTeams() throws Exception {
        when(teamService.getAllTeams()).thenReturn(List.of(testTeam));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(teamService).getAllTeams();
    }

    @Test
    void getTeams_withDivisionParam_filtersByDivision() throws Exception {
        when(teamService.getTeamsByDivision("AFC West")).thenReturn(List.of(testTeam));

        mockMvc.perform(get("/api/teams").param("division", "AFC West"))
                .andExpect(status().isOk());

        verify(teamService).getTeamsByDivision("AFC West");
        verify(teamService, never()).getTeamsByConference(any());
    }

    @Test
    void getTeams_withConferenceParam_filtersByConference() throws Exception {
        when(teamService.getTeamsByConference("AFC")).thenReturn(List.of(testTeam));

        mockMvc.perform(get("/api/teams").param("conference", "AFC"))
                .andExpect(status().isOk());

        verify(teamService).getTeamsByConference("AFC");
    }

    @Test
    void getTeams_divisionTakesPrecedenceOverConference() throws Exception {
        when(teamService.getTeamsByDivision("AFC West")).thenReturn(List.of(testTeam));

        mockMvc.perform(get("/api/teams")
                        .param("division", "AFC West")
                        .param("conference", "AFC"))
                .andExpect(status().isOk());

        verify(teamService).getTeamsByDivision("AFC West");
        verify(teamService, never()).getTeamsByConference(any());
    }

    @Test
    void saveTeam_returns200AndSavedTeam() throws Exception {
        when(teamService.saveTeam(any(Team.class))).thenReturn(testTeam);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTeam)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abbr").value("KC"));
    }
}
