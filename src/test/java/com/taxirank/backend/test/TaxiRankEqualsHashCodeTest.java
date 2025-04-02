package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.taxirank.backend.models.TaxiRank;

public class TaxiRankEqualsHashCodeTest {
    
    private TaxiRank rank1;
    private TaxiRank rank2;
    
    @BeforeEach
    void setUp() {
        rank1 = new TaxiRank();
        rank1.setId(1L);
        rank1.setName("Test Rank 1");
        rank1.setCode("TEST001");
        rank1.setAddress("123 Main St");
        rank1.setCity("Test City");
        rank1.setProvince("Test Province");
        
        rank2 = new TaxiRank();
        rank2.setId(1L);
        rank2.setName("Test Rank 1");
        rank2.setCode("TEST001");
        rank2.setAddress("123 Main St");
        rank2.setCity("Test City");
        rank2.setProvince("Test Province");
    }
    
    @Test
    void testEqualsWithSameObject() {
        assertTrue(rank1.equals(rank1));
    }
    
    @Test
    void testEqualsWithNull() {
        assertFalse(rank1.equals(null));
    }
    
    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(rank1.equals("Not a TaxiRank"));
    }
    
    @Test
    void testEqualsWithIdenticalProperties() {
        assertTrue(rank1.equals(rank2));
        assertTrue(rank2.equals(rank1));
    }
    
    @Test
    void testEqualsWithDifferentId() {
        rank2.setId(2L);
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testEqualsWithDifferentName() {
        rank2.setName("Different Name");
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testEqualsWithDifferentCode() {
        rank2.setCode("DIFF001");
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testEqualsWithDifferentAddress() {
        rank2.setAddress("456 Other St");
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testEqualsWithDifferentCity() {
        rank2.setCity("Other City");
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testEqualsWithDifferentProvince() {
        rank2.setProvince("Other Province");
        assertFalse(rank1.equals(rank2));
    }
    
    @Test
    void testHashCodeConsistency() {
        int hashCode1 = rank1.hashCode();
        int hashCode2 = rank1.hashCode();
        assertEquals(hashCode1, hashCode2);
    }
    
    @Test
    void testHashCodeWithEqualObjects() {
        int hashCode1 = rank1.hashCode();
        int hashCode2 = rank2.hashCode();
        assertEquals(hashCode1, hashCode2);
    }
    
    @Test
    void testHashCodeWithDifferentObjects() {
        rank2.setCode("DIFF001");
        int hashCode1 = rank1.hashCode();
        int hashCode2 = rank2.hashCode();
        assertNotEquals(hashCode1, hashCode2);
    }
} 