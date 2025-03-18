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
        testRank.setContactNumber("+27123456789");
        testRank.setOperatingHours("24/7");
        testRank.setStatus("ACTIVE");
        testRank.setLatitude(-33.9249);
        testRank.setLongitude(18.4241);
        testRank.setCapacity(50);
    }

    @Test
    public void shouldSaveRank() {
        // Save the rank
        Rank savedRank = rankRepository.save(testRank);

        // Verify the saved rank
        assertNotNull(savedRank.getId());
        assertEquals(testRank.getName(), savedRank.getName());
        assertEquals(testRank.getLocation(), savedRank.getLocation());
        assertEquals(testRank.getContactNumber(), savedRank.getContactNumber());
        assertEquals(testRank.getOperatingHours(), savedRank.getOperatingHours());
        assertEquals(testRank.getStatus(), savedRank.getStatus());
        assertEquals(testRank.getLatitude(), savedRank.getLatitude());
        assertEquals(testRank.getLongitude(), savedRank.getLongitude());
        assertEquals(testRank.getCapacity(), savedRank.getCapacity());
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
        assertEquals(savedRank.getLocation(), foundRank.getLocation());
        assertEquals(savedRank.getContactNumber(), foundRank.getContactNumber());
        assertEquals(savedRank.getOperatingHours(), foundRank.getOperatingHours());
        assertEquals(savedRank.getStatus(), foundRank.getStatus());
        assertEquals(savedRank.getLatitude(), foundRank.getLatitude());
        assertEquals(savedRank.getLongitude(), foundRank.getLongitude());
        assertEquals(savedRank.getCapacity(), foundRank.getCapacity());
    }
}
