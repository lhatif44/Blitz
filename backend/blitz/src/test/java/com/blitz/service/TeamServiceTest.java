package com.blitz.service;

import com.blitz.model.entity.Team;
import com.blitz.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team testTeam;

    @BeforeEach
    void setUp() {
        testTeam = new Team();
        testTeam.setAbbr("KC");
        testTeam.setFullName("Kansas City Chiefs");
        testTeam.setConference("AFC");
        testTeam.setDivision("AFC West");
    }

    @Test
    void getTeamByAbbr_returnsTeam_whenFound() {
        when(teamRepository.findById("KC")).thenReturn(Optional.of(testTeam));

        Team result = teamService.getTeamByAbbr("KC");

        assertThat(result.getFullName()).isEqualTo("Kansas City Chiefs");
        verify(teamRepository, times(1)).findById("KC");
    }

    @Test
    void getTeamByAbbr_throwsException_whenNotFound() {
        when(teamRepository.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamByAbbr("XX"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Team not found");
    }

    @Test
    void getAllTeams_returnsAllTeams() {
        Team team2 = new Team();
        team2.setAbbr("NE");
        team2.setFullName("New England Patriots");

        when(teamRepository.findAll()).thenReturn(List.of(testTeam, team2));

        List<Team> results = teamService.getAllTeams();

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Team::getAbbr).containsExactlyInAnyOrder("KC", "NE");
    }

    @Test
    void getTeamsByConference_returnsCorrectTeams() {
        when(teamRepository.findByConference("AFC")).thenReturn(List.of(testTeam));

        List<Team> results = teamService.getTeamsByConference("AFC");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getConference()).isEqualTo("AFC");
    }

    @Test
    void getTeamsByDivision_returnsCorrectTeams() {
        when(teamRepository.findByDivision("AFC West")).thenReturn(List.of(testTeam));

        List<Team> results = teamService.getTeamsByDivision("AFC West");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDivision()).isEqualTo("AFC West");
    }

    @Test
    void saveTeam_savesAndReturnsTeam() {
        when(teamRepository.save(testTeam)).thenReturn(testTeam);

        Team result = teamService.saveTeam(testTeam);

        assertThat(result.getAbbr()).isEqualTo("KC");
        verify(teamRepository, times(1)).save(testTeam);
    }
}
