package com.blitz.service;

import com.blitz.model.entity.*;
import com.blitz.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @Mock private PassingStatsRepository passingStatsRepository;
    @Mock private RushingStatsRepository rushingStatsRepository;
    @Mock private ReceivingStatsRepository receivingStatsRepository;
    @Mock private PassRushStatsRepository passRushStatsRepository;
    @Mock private LinebackerStatsRepository linebackerStatsRepository;
    @Mock private SecondaryStatsRepository secondaryStatsRepository;
    @Mock private KickingStatsRepository kickingStatsRepository;
    @Mock private PuntingStatsRepository puntingStatsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private UUID playerId;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
    }

    // --- Passing Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getPassingStats_withSeasonType_usesFilteredQuery() {
        PassingStats stats = new PassingStats();
        stats.setPassingYards(4000);
        when(passingStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<PassingStats> result = statsService.getPassingStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPassingYards()).isEqualTo(4000);
        verify(passingStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
        verify(passingStatsRepository, never()).findByPlayerId(any());
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getPassingStats_withNullSeasonType_returnsAllSeasons() {
        PassingStats stats = new PassingStats();
        stats.setPassingYards(4000);
        when(passingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of(stats));

        List<PassingStats> result = statsService.getPassingStats(playerId, null);

        assertThat(result).hasSize(1);
        verify(passingStatsRepository).findByPlayerId(playerId);
        verify(passingStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Rushing Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getRushingStats_withSeasonType_usesFilteredQuery() {
        RushingStats stats = new RushingStats();
        stats.setRushingYards(1200);
        when(rushingStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<RushingStats> result = statsService.getRushingStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRushingYards()).isEqualTo(1200);
        verify(rushingStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getRushingStats_withNullSeasonType_returnsAllSeasons() {
        when(rushingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getRushingStats(playerId, null);

        verify(rushingStatsRepository).findByPlayerId(playerId);
        verify(rushingStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Receiving Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getReceivingStats_withSeasonType_usesFilteredQuery() {
        ReceivingStats stats = new ReceivingStats();
        stats.setReceivingYards(900);
        when(receivingStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<ReceivingStats> result = statsService.getReceivingStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReceivingYards()).isEqualTo(900);
        verify(receivingStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getReceivingStats_withNullSeasonType_returnsAllSeasons() {
        when(receivingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getReceivingStats(playerId, null);

        verify(receivingStatsRepository).findByPlayerId(playerId);
        verify(receivingStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Pass Rush Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getPassRushStats_withSeasonType_usesFilteredQuery() {
        PassRushStats stats = new PassRushStats();
        stats.setQbHits(20);
        when(passRushStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<PassRushStats> result = statsService.getPassRushStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQbHits()).isEqualTo(20);
        verify(passRushStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getPassRushStats_withNullSeasonType_returnsAllSeasons() {
        when(passRushStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getPassRushStats(playerId, null);

        verify(passRushStatsRepository).findByPlayerId(playerId);
        verify(passRushStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Linebacker Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getLinebackerStats_withSeasonType_usesFilteredQuery() {
        LinebackerStats stats = new LinebackerStats();
        stats.setTacklesTotal(120);
        when(linebackerStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<LinebackerStats> result = statsService.getLinebackerStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTacklesTotal()).isEqualTo(120);
        verify(linebackerStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getLinebackerStats_withNullSeasonType_returnsAllSeasons() {
        when(linebackerStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getLinebackerStats(playerId, null);

        verify(linebackerStatsRepository).findByPlayerId(playerId);
        verify(linebackerStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Secondary Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getSecondaryStats_withSeasonType_usesFilteredQuery() {
        SecondaryStats stats = new SecondaryStats();
        stats.setInterceptions(5);
        when(secondaryStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<SecondaryStats> result = statsService.getSecondaryStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInterceptions()).isEqualTo(5);
        verify(secondaryStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getSecondaryStats_withNullSeasonType_returnsAllSeasons() {
        when(secondaryStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getSecondaryStats(playerId, null);

        verify(secondaryStatsRepository).findByPlayerId(playerId);
        verify(secondaryStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Kicking Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getKickingStats_withSeasonType_usesFilteredQuery() {
        KickingStats stats = new KickingStats();
        stats.setFgMade(30);
        when(kickingStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<KickingStats> result = statsService.getKickingStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFgMade()).isEqualTo(30);
        verify(kickingStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getKickingStats_withNullSeasonType_returnsAllSeasons() {
        when(kickingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getKickingStats(playerId, null);

        verify(kickingStatsRepository).findByPlayerId(playerId);
        verify(kickingStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Punting Stats ---

    @Test
    //When a seasonType is provided, the repository is called with both playerId and seasonType
    void getPuntingStats_withSeasonType_usesFilteredQuery() {
        PuntingStats stats = new PuntingStats();
        stats.setPunts(60);
        when(puntingStatsRepository.findByPlayerIdAndSeasonType(playerId, "REG")).thenReturn(List.of(stats));

        List<PuntingStats> result = statsService.getPuntingStats(playerId, "REG");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPunts()).isEqualTo(60);
        verify(puntingStatsRepository).findByPlayerIdAndSeasonType(playerId, "REG");
    }

    @Test
    //When seasonType is null, the unfiltered query is used and all season rows are returned
    void getPuntingStats_withNullSeasonType_returnsAllSeasons() {
        when(puntingStatsRepository.findByPlayerId(playerId)).thenReturn(List.of());

        statsService.getPuntingStats(playerId, null);

        verify(puntingStatsRepository).findByPlayerId(playerId);
        verify(puntingStatsRepository, never()).findByPlayerIdAndSeasonType(any(), any());
    }

    // --- Save ---

    @Test
    //Function that writes a passing stats row and returns the saved entity
    void savePassingStats_savesAndReturnsStats() {
        PassingStats stats = new PassingStats();
        stats.setPassingYards(4000);
        when(passingStatsRepository.save(stats)).thenReturn(stats);

        PassingStats result = statsService.savePassingStats(stats);

        assertThat(result.getPassingYards()).isEqualTo(4000);
        verify(passingStatsRepository).save(stats);
    }
}
