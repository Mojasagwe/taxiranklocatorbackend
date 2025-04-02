package com.taxirank.backend.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.taxirank.backend.dto.RankAdminAssignmentDTO;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.RankAdmin;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.impl.RankAdminServiceImpl;

public class RankAdminServiceTest {

    @Mock
    private RankAdminRepository rankAdminRepository;
    
    @Mock
    private TaxiRankRepository taxiRankRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private RankAdminServiceImpl rankAdminService;
    
    private User adminUser;
    private TaxiRank taxiRank;
    private RankAdmin rankAdmin;
    private RankAdminAssignmentDTO assignmentDTO;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test admin user
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPhoneNumber("1234567890");
        adminUser.setRole(UserRole.ADMIN);
        
        // Setup test taxi rank
        taxiRank = new TaxiRank();
        taxiRank.setId(1L);
        taxiRank.setName("Test Rank");
        taxiRank.setCode("TEST001");
        taxiRank.setAddress("Test Location");
        
        // Setup test rank admin
        rankAdmin = new RankAdmin();
        rankAdmin.setId(1L);
        rankAdmin.setUser(adminUser);
        rankAdmin.setTaxiRank(taxiRank);
        rankAdmin.setDesignation("Test Admin");
        rankAdmin.setCanEditRankDetails(true);
        rankAdmin.setCanManageRoutes(true);
        rankAdmin.setCanManageDrivers(true);
        rankAdmin.setCanViewFinancials(true);
        
        // Setup assignment DTO
        assignmentDTO = new RankAdminAssignmentDTO();
        assignmentDTO.setUserId(1L);
        assignmentDTO.setRankCode("TEST001");
        assignmentDTO.setDesignation("Test Admin");
        assignmentDTO.setCanEditRankDetails(true);
        assignmentDTO.setCanManageRoutes(true);
        assignmentDTO.setCanManageDrivers(true);
        assignmentDTO.setCanViewFinancials(true);
    }
    
    @Test
    void testAssignAdminToRank_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findByCode("TEST001")).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.existsByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(false);
        when(rankAdminRepository.countByTaxiRank(taxiRank)).thenReturn(0L);
        when(rankAdminRepository.save(any(RankAdmin.class))).thenReturn(rankAdmin);
        
        // Act
        RankAdmin result = rankAdminService.assignAdminToRank(assignmentDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals(adminUser, result.getUser());
        assertEquals(taxiRank, result.getTaxiRank());
        assertEquals("Test Admin", result.getDesignation());
        verify(rankAdminRepository, times(1)).save(any(RankAdmin.class));
    }
    
    @Test
    void testAssignAdminToRank_UserNotFound() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assignmentDTO.setUserId(2L);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rankAdminService.assignAdminToRank(assignmentDTO);
        });
        verify(userRepository, times(1)).findById(2L);
    }
    
    @Test
    void testAssignAdminToRank_RankNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());
        assignmentDTO.setRankCode("NONEXISTENT");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rankAdminService.assignAdminToRank(assignmentDTO);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findByCode("NONEXISTENT");
    }
    
    @Test
    void testGetRanksManagedByAdmin() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(rankAdminRepository.findByUser(adminUser)).thenReturn(Collections.singletonList(rankAdmin));
        
        // Act
        List<TaxiRank> ranks = rankAdminService.getRanksManagedByAdmin(1L);
        
        // Assert
        assertEquals(1, ranks.size());
        assertEquals(taxiRank.getId(), ranks.get(0).getId());
        verify(userRepository, times(1)).findById(1L);
        verify(rankAdminRepository, times(1)).findByUser(adminUser);
    }
    
    @Test
    void testGetAdminsForRank() {
        // Arrange
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.findByTaxiRank(taxiRank)).thenReturn(Collections.singletonList(rankAdmin));
        
        // Act
        List<User> admins = rankAdminService.getAdminsForRank(1L);
        
        // Assert
        assertEquals(1, admins.size());
        assertEquals(adminUser.getId(), admins.get(0).getId());
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(rankAdminRepository, times(1)).findByTaxiRank(taxiRank);
    }
    
    @Test
    void testGetAdminsForRankByCode() {
        // Arrange
        when(taxiRankRepository.findByCode("TEST001")).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.findByTaxiRank(taxiRank)).thenReturn(Collections.singletonList(rankAdmin));
        
        // Act
        List<User> admins = rankAdminService.getAdminsForRankByCode("TEST001");
        
        // Assert
        assertEquals(1, admins.size());
        assertEquals(adminUser.getId(), admins.get(0).getId());
        verify(taxiRankRepository, times(1)).findByCode("TEST001");
        verify(rankAdminRepository, times(1)).findByTaxiRank(taxiRank);
    }
    
    @Test
    void testIsUserAdminForRank_True() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.existsByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(true);
        
        // Act
        boolean result = rankAdminService.isUserAdminForRank(1L, 1L);
        
        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(rankAdminRepository, times(1)).existsByUserAndTaxiRank(adminUser, taxiRank);
    }
    
    @Test
    void testIsUserAdminForRank_False() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findById(2L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.existsByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(false);
        
        // Act
        boolean result = rankAdminService.isUserAdminForRank(1L, 2L);
        
        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findById(2L);
        verify(rankAdminRepository, times(1)).existsByUserAndTaxiRank(adminUser, taxiRank);
    }
    
    @Test
    void testRemoveAdminFromRank() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.findByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(Optional.of(rankAdmin));
        when(rankAdminRepository.findByUser(adminUser)).thenReturn(Collections.emptyList());
        doNothing().when(rankAdminRepository).delete(rankAdmin);
        
        // Act
        rankAdminService.removeAdminFromRank(1L, 1L);
        
        // Assert
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(rankAdminRepository, times(1)).findByUserAndTaxiRank(adminUser, taxiRank);
        verify(rankAdminRepository, times(1)).delete(rankAdmin);
    }
    
    @Test
    void testRemoveAdminFromRank_NotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findById(2L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.findByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rankAdminService.removeAdminFromRank(1L, 2L);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findById(2L);
    }
    
    @Test
    void testUpdateAdminPermissions() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(taxiRankRepository.findById(1L)).thenReturn(Optional.of(taxiRank));
        when(rankAdminRepository.findByUserAndTaxiRank(adminUser, taxiRank)).thenReturn(Optional.of(rankAdmin));
        when(rankAdminRepository.save(any(RankAdmin.class))).thenReturn(rankAdmin);
        
        RankAdminAssignmentDTO updateDTO = new RankAdminAssignmentDTO();
        updateDTO.setDesignation("Updated Designation");
        updateDTO.setCanEditRankDetails(false);
        updateDTO.setCanManageRoutes(false);
        
        // Act
        RankAdmin result = rankAdminService.updateAdminPermissions(1L, 1L, updateDTO);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated Designation", result.getDesignation());
        assertFalse(result.isCanEditRankDetails());
        assertFalse(result.isCanManageRoutes());
        verify(userRepository, times(1)).findById(1L);
        verify(taxiRankRepository, times(1)).findById(1L);
        verify(rankAdminRepository, times(1)).findByUserAndTaxiRank(adminUser, taxiRank);
        verify(rankAdminRepository, times(1)).save(rankAdmin);
    }
} 