package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.taxirank.backend.models.Rank;
import com.taxirank.backend.repositories.RankRepository;

@DataJpaTest
@ActiveProfiles("test")
public class RankRepositoryTest {

    @Autowired
    private RankRepository rankRepository;

    private Rank testRank;

    @BeforeEach
    void setUp() {
        testRank = new Rank();
        testRank.setName("Test Rank");
        testRank.setLocation("Test Location");
        // Set other required fields based on your Rank entity
    }

    @Test
    public void shouldSaveRank() {
        // Save the rank
        Rank savedRank = rankRepository.save(testRank);

        // Verify the saved rank
        assertNotNull(savedRank.getId());
        assertEquals(testRank.getName(), savedRank.getName());
        assertEquals(testRank.getLocation(), savedRank.getLocation());
    }

    @Test
    public void shouldFindRankById() {
        // Save the rank first
        Rank savedRank = rankRepository.save(testRank);

        // Find the rank by ID
        Rank foundRank = rankRepository.findById(savedRank.getId()).orElse(null);

        // Verify
        assertNotNull(foundRank);
        assertEquals(savedRank.getName(), foundRank.getName());
    }
}
