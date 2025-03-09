package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.taxirank.backend.models.Rank;
import com.taxirank.backend.repositories.RankRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class RankRepositoryTest {

    @Autowired
    private RankRepository rankRepository;

    @Test
    public void shouldSaveRank() {
        // Create a test rank
        Rank rank = new Rank();
        rank.setName("Test Rank");
        rank.setLocation("Test Location");
        rank.setCapacity(10);

        // Save the rank
        Rank savedRank = rankRepository.save(rank);

        // Verify the saved rank
        assertNotNull(savedRank.getId());
        assertEquals("Test Rank", savedRank.getName());
        assertEquals("Test Location", savedRank.getLocation());
        assertEquals(10, savedRank.getCapacity());
    }

    @Test
    public void shouldFindRankById() {
        // Create and save a test rank
        Rank rank = new Rank();
        rank.setName("Test Rank");
        rank.setLocation("Test Location");
        rank.setCapacity(10);
        Rank savedRank = rankRepository.save(rank);

        // Find the rank by ID
        Rank foundRank = rankRepository.findById(savedRank.getId()).orElse(null);

        // Verify the found rank
        assertNotNull(foundRank);
        assertEquals(savedRank.getId(), foundRank.getId());
        assertEquals(savedRank.getName(), foundRank.getName());
    }

    @Test
    public void shouldDeleteRank() {
        // Create and save a test rank
        Rank rank = new Rank();
        rank.setName("Test Rank");
        rank.setLocation("Test Location");
        rank.setCapacity(10);
        Rank savedRank = rankRepository.save(rank);

        // Delete the rank
        rankRepository.deleteById(savedRank.getId());

        // Verify the rank is deleted
        assertFalse(rankRepository.existsById(savedRank.getId()));
    }
}