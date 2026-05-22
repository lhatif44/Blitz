package com.blitz.service;

import com.blitz.model.entity.PassingStats;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.SecondaryStats;
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

@ExtendWith(MockitoExtension.class)
public class CompareServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private StatsService statsService;

    @InjectMocks
    private CompareServiceImpl compareService;

    private UUID qbId1;
    private UUID qbId2;
    private UUID cbId;
    private Player qb1;
    private Player qb2;
    private Player cb;

    @BeforeEach
    void setUp() {
        qbId1 = UUID.randomUUID();
        qbId2 = UUID.randomUUID();
        cbId = UUID.randomUUID();

        qb1 = new Player();
        qb1.setNflverseId("qb-001");
        qb1.setDisplayName("Patrick Mahomes");
        qb1.setPositionGroup("QB");

        qb2 = new Player();
        qb2.setNflverseId("qb-002");
        qb2.setDisplayName("Josh Allen");
        qb2.setPositionGroup("QB");

        cb = new Player();
        cb.setNflverseId("cb-001");
        cb.setDisplayName("Jalen Ramsey");
        cb.setPositionGroup("CB");
    }

    @Test
    void comparePlayers_returnsResult_whenSamePositionGroup() {
        PassingStats stats1 = new PassingStats();
        stats1.setPassingYards(5000);

        PassingStats stats2 = new PassingStats();
        stats2.setPassingYards(4500);

        when(playerRepository.findById(qbId1)).thenReturn(Optional.of(qb1));
        when(playerRepository.findById(qbId2)).thenReturn(Optional.of(qb2));
        when(statsService.getPassingStats(qbId1, "REG")).thenReturn(List.of(stats1));
        when(statsService.getPassingStats(qbId2, "REG")).thenReturn(List.of(stats2));

        CompareService.ComparisonResult result = compareService.comparePlayers(qbId1, qbId2, "REG");

        assertThat(result.getPlayer1().getDisplayName()).isEqualTo("Patrick Mahomes");
        assertThat(result.getPlayer2().getDisplayName()).isEqualTo("Josh Allen");
        assertThat(result.getPositionGroup()).isEqualTo("QB");
        assertThat(result.getPlayer1Stats()).isNotNull();
        assertThat(result.getPlayer2Stats()).isNotNull();
    }

    @Test
    void comparePlayers_throwsException_whenDifferentPositionGroups() {
        when(playerRepository.findById(qbId1)).thenReturn(Optional.of(qb1));
        when(playerRepository.findById(cbId)).thenReturn(Optional.of(cb));

        assertThatThrownBy(() -> compareService.comparePlayers(qbId1, cbId, "REG"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot compare players from different position groups");
    }

    @Test
    void comparePlayers_throwsException_whenPlayerNotFound() {
        when(playerRepository.findById(qbId1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compareService.comparePlayers(qbId1, qbId2, "REG"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Player not found");
    }

    @Test
    void validateSamePositionGroup_throwsException_whenDifferentPositionGroups() {
        when(playerRepository.findById(qbId1)).thenReturn(Optional.of(qb1));
        when(playerRepository.findById(cbId)).thenReturn(Optional.of(cb));

        assertThatThrownBy(() -> compareService.validateSamePositionGroup(qbId1, cbId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot compare players from different position groups");
    }

    @Test
    void comparePlayers_fetchesCorrectStatsForCBs() {
        UUID cbId2 = UUID.randomUUID();
        Player cb2 = new Player();
        cb2.setNflverseId("cb-002");
        cb2.setDisplayName("Sauce Gardner");
        cb2.setPositionGroup("CB");

        SecondaryStats stats1 = new SecondaryStats();
        stats1.setInterceptions(6);

        SecondaryStats stats2 = new SecondaryStats();
        stats2.setInterceptions(4);

        when(playerRepository.findById(cbId)).thenReturn(Optional.of(cb));
        when(playerRepository.findById(cbId2)).thenReturn(Optional.of(cb2));
        when(statsService.getSecondaryStats(cbId, "REG")).thenReturn(List.of(stats1));
        when(statsService.getSecondaryStats(cbId2, "REG")).thenReturn(List.of(stats2));

        CompareService.ComparisonResult result = compareService.comparePlayers(cbId, cbId2, "REG");

        assertThat(result.getPositionGroup()).isEqualTo("CB");
        assertThat(result.getPlayer1Stats()).isNotNull();
    }
}
