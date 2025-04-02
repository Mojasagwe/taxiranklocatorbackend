package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.services.impl.TaxiRankServiceImpl;

public class TaxiRankServiceTest {

    @Mock
    private TaxiRankRepository taxiRankRepository;
    
    @Mock
    private RankAdminRepository rankAdminRepository;
    
    @InjectMocks
    private TaxiRankServiceImpl taxiRankService;
    
    private TaxiRank testRank1;
    private TaxiRank testRank2;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testRank1 = new TaxiRank();
        testRank1.setId(1L);
        testRank1.setName("Test Rank 1");
        testRank1.setCode("TEST001");
        testRank1.setAddress("Test Location 1");
        testRank1.setContactPhone("+27123456789");
        testRank1.setOperatingHours("24/7");
        testRank1.setIsActive(true);
        testRank1.setLatitude(-33.9249);
        testRank1.setLongitude(18.4241);
        testRank1.setCapacity(50);
        testRank1.setCity("Test City");
        testRank1.setProvince("Test Province");
        
        testRank2 = new TaxiRank();
        testRank2.setId(2L);
        testRank2.setName("Test Rank 2");
        testRank2.setCode("TEST002");
        testRank2.setAddress("Test Location 2");
        testRank2.setContactPhone("+27987654321");
        testRank2.setOperatingHours("24/7");
        testRank2.setIsActive(true);
        testRank2.setLatitude(-33.8249);
        testRank2.setLongitude(18.3241);
        testRank2.setCapacity(100);
        testRank2.setCity("Test City 2");
        testRank2.setProvince("Test Province 2");
    }
    
    @Test
    void testGetAllRanks() {
        // Arrange
        when(taxiRankRepository.findAll()).thenReturn(Arrays.asList(testRank1, testRank2));
        
        // Act
        List<TaxiRank> ranks = taxiRankService.getAllRanks();
        
        // Assert
        assertEquals(2, ranks.size());
        verify(taxiRankRepository, times(1)).findAll();
    }
    
    @Test
    void testGetUnassignedRanks() {
        // Arrange
        when(taxiRankRepository.findUnassignedRanks()).thenReturn(Arrays.asList(testRank2));
        
        // Act
        List<TaxiRank> unassignedRanks = taxiRankService.getUnassignedRanks();
        
        // Assert
        assertEquals(1, unassignedRanks.size());
        assertEquals(testRank2.getId(), unassignedRanks.get(0).getId());
        verify(taxiRankRepository, times(1)).findUnassignedRanks();
    }
    
    @Test
    void testGetRankById_Success() {
        // Arrange
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(testRank1));
        
        // Act
        TaxiRank rank = taxiRankService.getRankById(1L);
        
        // Assert
        assertNotNull(rank);
        assertEquals(testRank1.getId(), rank.getId());
        assertEquals(testRank1.getName(), rank.getName());
        verify(taxiRankRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetRankById_NotFound() {
        // Arrange
        when(taxiRankRepository.findById(3L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taxiRankService.getRankById(3L);
        });
        verify(taxiRankRepository, times(1)).findById(3L);
    }
    
    @Test
    void testGetRankByCode_Success() {
        // Arrange
        when(taxiRankRepository.findByCode("TEST001")).thenReturn(Optional.of(testRank1));
        
        // Act
        TaxiRank rank = taxiRankService.getRankByCode("TEST001");
        
        // Assert
        assertNotNull(rank);
        assertEquals(testRank1.getId(), rank.getId());
        assertEquals(testRank1.getCode(), rank.getCode());
        verify(taxiRankRepository, times(1)).findByCode("TEST001");
    }
    
    @Test
    void testGetRankByCode_NotFound() {
        // Arrange
        when(taxiRankRepository.findByCode("INVALID")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taxiRankService.getRankByCode("INVALID");
        });
        verify(taxiRankRepository, times(1)).findByCode("INVALID");
    }
    
    @Test
    void testCreateRank() {
        // Arrange
        when(taxiRankRepository.save(any(TaxiRank.class))).thenReturn(testRank1);
        
        // Act
        TaxiRank createdRank = taxiRankService.createRank(testRank1);
        
        // Assert
        assertNotNull(createdRank);
        assertEquals(testRank1.getName(), createdRank.getName());
        verify(taxiRankRepository, times(1)).save(testRank1);
    }
    
    @Test
    void testUpdateRank_Success() {
        // Arrange
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(testRank1));
        when(taxiRankRepository.save(any(TaxiRank.class))).thenReturn(testRank1);
        
        TaxiRank updatedDetails = new TaxiRank();
        updatedDetails.setName("Updated Name");
        updatedDetails.setCode("UPD001");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setAddress("Updated Address");
        
        // Act
        TaxiRank result = taxiRankService.updateRank(1L, updatedDetails);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("UPD001", result.getCode());
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).save(any(TaxiRank.class));
    }
    
    @Test
    void testUpdateRank_NotFound() {
        // Arrange
        when(taxiRankRepository.findById(3L)).thenReturn(Optional.empty());
        
        TaxiRank updatedDetails = new TaxiRank();
        updatedDetails.setName("Updated Name");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taxiRankService.updateRank(3L, updatedDetails);
        });
        verify(taxiRankRepository, times(1)).findById(3L);
    }
    
    @Test
    void testDeleteRank_Success() {
        // Arrange
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(testRank1));
        doNothing().when(taxiRankRepository).delete(testRank1);
        
        // Act
        taxiRankService.deleteRank(1L);
        
        // Assert
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).delete(testRank1);
    }
    
    @Test
    void testDeleteRank_NotFound() {
        // Arrange
        when(taxiRankRepository.findById(3L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            taxiRankService.deleteRank(3L);
        });
        verify(taxiRankRepository, times(1)).findById(3L);
    }
} 