package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.TaxiRankRepository;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class RankRepositoryTest {

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    private TaxiRank testRank;
    private TaxiRank testRank2;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        taxiRankRepository.deleteAll();
        
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
        
        testRank2 = new TaxiRank();
        testRank2.setName("Second Rank");
        testRank2.setCode("TEST002");
        testRank2.setAddress("Another Location");
        testRank2.setContactPhone("+27987654321");
        testRank2.setOperatingHours("08:00-18:00");
        testRank2.setIsActive(true);
        testRank2.setLatitude(-33.8249);
        testRank2.setLongitude(18.3241);
        testRank2.setCapacity(30);
        testRank2.setCity("Other City");
        testRank2.setProvince("Other Province");
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
    
    @Test
    public void shouldFindRankByCode() {
        // Save the rank first
        taxiRankRepository.save(testRank);
        
        // Find the rank by code
        Optional<TaxiRank> foundRankOpt = taxiRankRepository.findByCode("TEST001");
        
        // Verify
        assertTrue(foundRankOpt.isPresent());
        TaxiRank foundRank = foundRankOpt.get();
        assertEquals(testRank.getName(), foundRank.getName());
        assertEquals(testRank.getCode(), foundRank.getCode());
    }
    
    @Test
    public void shouldReturnEmptyOptionalForNonExistentCode() {
        // Attempt to find a rank with a non-existent code
        Optional<TaxiRank> foundRankOpt = taxiRankRepository.findByCode("NONEXISTENT");
        
        // Verify
        assertFalse(foundRankOpt.isPresent());
    }
    
    @Test
    public void shouldFindAllRanks() {
        // Save both test ranks
        taxiRankRepository.save(testRank);
        taxiRankRepository.save(testRank2);
        
        // Find all ranks
        List<TaxiRank> allRanks = taxiRankRepository.findAll();
        
        // Verify
        assertEquals(2, allRanks.size());
    }
    
    @Test
    public void shouldDeleteRank() {
        // Save the rank first
        TaxiRank savedRank = taxiRankRepository.save(testRank);
        
        // Delete the rank
        taxiRankRepository.delete(savedRank);
        
        // Verify it was deleted
        Optional<TaxiRank> foundRankOpt = taxiRankRepository.findById(savedRank.getId());
        assertFalse(foundRankOpt.isPresent());
    }
    
    @Test
    public void shouldCheckIfCodeExists() {
        // Save the rank first
        taxiRankRepository.save(testRank);
        
        // Check if code exists
        boolean exists = taxiRankRepository.existsByCode("TEST001");
        boolean nonExistentExists = taxiRankRepository.existsByCode("NONEXISTENT");
        
        // Verify
        assertTrue(exists);
        assertFalse(nonExistentExists);
    }
    
    @Test
    public void shouldUpdateRank() {
        // Save the rank first
        TaxiRank savedRank = taxiRankRepository.save(testRank);
        
        // Update the rank
        savedRank.setName("Updated Name");
        savedRank.setCapacity(100);
        TaxiRank updatedRank = taxiRankRepository.save(savedRank);
        
        // Verify the updates
        assertEquals("Updated Name", updatedRank.getName());
        assertEquals(100, updatedRank.getCapacity());
        
        // Make sure it's still the same record
        assertEquals(savedRank.getId(), updatedRank.getId());
        assertEquals(savedRank.getCode(), updatedRank.getCode());
    }
}
