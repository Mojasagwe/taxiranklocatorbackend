package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.TaxiRankRepository;

@DataJpaTest
@ActiveProfiles("test")
public class RankRepositoryTest {

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    private TaxiRank testRank;

    @BeforeEach
    void setUp() {
        testRank = new TaxiRank();
        testRank.setName("Test Rank");
        testRank.setCode("TEST001");
        testRank.setAddress("Test Location");
        testRank.setContactPhone("+27123456789");
        testRank.setOperatingHours("24/7");
        testRank.setIsActive(true);
        testRank.setLatitude(-33.9249);
        testRank.setLongitude(18.4241);
        testRank.setCapacity(50);
        testRank.setCity("Test City");
        testRank.setProvince("Test Province");
    }

    @Test
    public void shouldSaveRank() {
        // Save the rank
        TaxiRank savedRank = taxiRankRepository.save(testRank);

        // Verify the saved rank
        assertNotNull(savedRank.getId());
        assertEquals(testRank.getName(), savedRank.getName());
        assertEquals(testRank.getCode(), savedRank.getCode());
        assertEquals(testRank.getAddress(), savedRank.getAddress());
        assertEquals(testRank.getContactPhone(), savedRank.getContactPhone());
        assertEquals(testRank.getOperatingHours(), savedRank.getOperatingHours());
        assertEquals(testRank.getIsActive(), savedRank.getIsActive());
        assertEquals(testRank.getLatitude(), savedRank.getLatitude());
        assertEquals(testRank.getLongitude(), savedRank.getLongitude());
        assertEquals(testRank.getCapacity(), savedRank.getCapacity());
    }

    @Test
    public void shouldFindRankById() {
        // Save the rank first
        TaxiRank savedRank = taxiRankRepository.save(testRank);

        // Find the rank by ID
        TaxiRank foundRank = taxiRankRepository.findById(savedRank.getId()).orElse(null);

        // Verify
        assertNotNull(foundRank);
        assertEquals(savedRank.getName(), foundRank.getName());
        assertEquals(savedRank.getCode(), foundRank.getCode());
        assertEquals(savedRank.getAddress(), foundRank.getAddress());
        assertEquals(savedRank.getContactPhone(), foundRank.getContactPhone());
        assertEquals(savedRank.getOperatingHours(), foundRank.getOperatingHours());
        assertEquals(savedRank.getIsActive(), foundRank.getIsActive());
        assertEquals(savedRank.getLatitude(), foundRank.getLatitude());
        assertEquals(savedRank.getLongitude(), foundRank.getLongitude());
        assertEquals(savedRank.getCapacity(), foundRank.getCapacity());
    }
}
