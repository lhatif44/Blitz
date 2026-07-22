package com.blitz.repository;

import com.blitz.model.entity.Player;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// loads only JPA layer =
@DataJpaTest
// use the real PostgreSQL database=
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void savesAndRetrievesPlayer() {
        // create a test player
        Player player = new Player();
        player.setNflverseId("test-001");
        player.setDisplayName("Test Player");
        player.setFirstName("Test");
        player.setLastName("Player");
        player.setPosition("QB");
        player.setPositionGroup("QB");
        player.setStatus("Active");

        // save to database
        Player saved = playerRepository.save(player);

        // verify it was saved with a generated ID
        assertThat(saved.getId()).isNotNull();

        // retrieve by ID and verify fields match
        Optional<Player> found = playerRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Test Player");
        assertThat(found.get().getPositionGroup()).isEqualTo("QB");
    }

    @Test
    void findsPlayerByNflverseId() {
        Player player = new Player();
        player.setNflverseId("test-002");
        player.setDisplayName("Another Player");
        player.setStatus("Retired");
        playerRepository.save(player);

        Optional<Player> found = playerRepository.findByNflverseId("test-002");
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Another Player");
    }

    @Test
    void searchesPlayersByNameCaseInsensitive() {
        Player player = new Player();
        player.setNflverseId("test-003");
        player.setDisplayName("Patrick Mahomes");
        player.setPositionGroup("QB");
        playerRepository.save(player);

        // search with lowercase — should still find the player
        List<Player> results = playerRepository.findByDisplayNameContainingIgnoreCase("mahomes");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDisplayName()).isEqualTo("Patrick Mahomes");
    }

    @Test
    void findsPlayersByPositionGroup() {
        Player qb = new Player();
        qb.setNflverseId("test-004");
        qb.setDisplayName("QB Player");
        qb.setPositionGroup("QB");
        playerRepository.save(qb);

        Player cb = new Player();
        cb.setNflverseId("test-005");
        cb.setDisplayName("CB Player");
        cb.setPositionGroup("CB");
        playerRepository.save(cb);

        List<Player> qbs = playerRepository.findByPositionGroup("QB");
        assertThat(qbs).anyMatch(p -> p.getNflverseId().equals("test-004"));

        List<Player> cbs = playerRepository.findByPositionGroup("CB");
        assertThat(cbs).anyMatch(p -> p.getNflverseId().equals("test-005"));
    }
}
