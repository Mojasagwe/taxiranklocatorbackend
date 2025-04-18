package com.taxirank.backend.test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxirank.backend.controllers.TaxiRankController;
import com.taxirank.backend.dto.TaxiRankDTO;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TaxiRankService;
import com.taxirank.backend.services.TerminalService;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.utils.TaxiRankMapper;

@WebMvcTest(TaxiRankController.class)
public class TaxiRankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaxiRankService taxiRankService;
    
    @MockBean
    private RankAdminService rankAdminService;
    
    @MockBean
    private TerminalService terminalService;
    
    @MockBean
    private TaxiRankMapper rankMapper;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private TaxiRank testRank1;
    private TaxiRank testRank2;
    private TaxiRankDTO testRankDTO1;
    private TaxiRankDTO testRankDTO2;
    private List<TaxiRankDTO> rankDTOs;

    @BeforeEach
    void setUp() {
        testRank1 = new TaxiRank();
        testRank1.setId(1L);
        testRank1.setName("Test Rank 1");
        testRank1.setCode("TEST001");
        testRank1.setAddress("Test Location 1");
        testRank1.setContactPhone("+27123456789");
        testRank1.setOperatingHours("05:00-22:00");
        testRank1.setIsActive(true);
        testRank1.setLatitude(-33.9249);
        testRank1.setLongitude(18.4241);
        testRank1.setCapacity(50);
        testRank1.setCity("Test City 1");
        testRank1.setProvince("Test Province 1");

        testRank2 = new TaxiRank();
        testRank2.setId(2L);
        testRank2.setName("Test Rank 2");
        testRank2.setCode("TEST002");
        testRank2.setAddress("Test Location 2");
        testRank2.setContactPhone("+27987654321");
        testRank2.setOperatingHours("06:00-23:00");
        testRank2.setIsActive(true);
        testRank2.setLatitude(-33.8249);
        testRank2.setLongitude(18.3241);
        testRank2.setCapacity(100);
        testRank2.setCity("Test City 2");
        testRank2.setProvince("Test Province 2");
        
        // Create DTOs for testing
        testRankDTO1 = new TaxiRankDTO();
        testRankDTO1.setId(1L);
        testRankDTO1.setName("Test Rank 1");
        testRankDTO1.setCode("TEST001");
        testRankDTO1.setAddress("Test Location 1");
        testRankDTO1.setContactPhone("+27123456789");
        testRankDTO1.setOperatingHours("05:00-22:00");
        testRankDTO1.setIsActive(true);
        testRankDTO1.setLatitude(-33.9249);
        testRankDTO1.setLongitude(18.4241);
        testRankDTO1.setCapacity(50);
        testRankDTO1.setCity("Test City 1");
        testRankDTO1.setProvince("Test Province 1");
        
        testRankDTO2 = new TaxiRankDTO();
        testRankDTO2.setId(2L);
        testRankDTO2.setName("Test Rank 2");
        testRankDTO2.setCode("TEST002");
        testRankDTO2.setAddress("Test Location 2");
        testRankDTO2.setContactPhone("+27987654321");
        testRankDTO2.setOperatingHours("06:00-23:00");
        testRankDTO2.setIsActive(true);
        testRankDTO2.setLatitude(-33.8249);
        testRankDTO2.setLongitude(18.3241);
        testRankDTO2.setCapacity(100);
        testRankDTO2.setCity("Test City 2");
        testRankDTO2.setProvince("Test Province 2");
        
        rankDTOs = Arrays.asList(testRankDTO1, testRankDTO2);
        
        // Set up rankMapper mocks
        when(rankMapper.toDTO(any(TaxiRank.class), anyBoolean(), anyBoolean())).thenAnswer(invocation -> {
            TaxiRank rank = invocation.getArgument(0);
            if (rank.getId() == 1L) {
                return testRankDTO1;
            } else if (rank.getId() == 2L) {
                return testRankDTO2;
            }
            return null;
        });
        
        when(rankMapper.toDTOList(anyList(), anyBoolean(), anyBoolean())).thenReturn(rankDTOs);
        
        when(rankMapper.createEntityFromDTO(any(TaxiRankDTO.class))).thenAnswer(invocation -> {
            TaxiRankDTO dto = invocation.getArgument(0);
            if (dto.getId() == 1L) {
                return testRank1;
            } else if (dto.getId() == 2L) {
                return testRank2;
            }
            return testRank1; // Default to testRank1 for simplicity
        });
    }

    @Test
    @WithMockUser
    public void shouldGetAllRanks() throws Exception {
        List<TaxiRank> ranks = Arrays.asList(testRank1, testRank2);
        when(taxiRankService.getAllRanks()).thenReturn(ranks);

        mockMvc.perform(get("/api/taxi-ranks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All taxi ranks retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Rank 1"))
                .andExpect(jsonPath("$.data[0].code").value("TEST001"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Test Rank 2"))
                .andExpect(jsonPath("$.data[1].code").value("TEST002"));

        verify(taxiRankService, times(1)).getAllRanks();
        verify(rankMapper, times(1)).toDTOList(anyList(), anyBoolean(), anyBoolean());
    }

    @Test
    @WithMockUser
    public void shouldGetRankById() throws Exception {
        when(taxiRankService.getRankById(1L)).thenReturn(testRank1);

        mockMvc.perform(get("/api/taxi-ranks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Rank 1"))
                .andExpect(jsonPath("$.data.code").value("TEST001"));

        verify(taxiRankService, times(1)).getRankById(1L);
        verify(rankMapper, times(1)).toDTO(any(TaxiRank.class), anyBoolean(), anyBoolean());
    }

    @Test
    @WithMockUser
    public void shouldReturnNotFoundForNonExistentRank() throws Exception {
        when(taxiRankService.getRankById(999L)).thenThrow(new RuntimeException("Taxi rank not found"));

        mockMvc.perform(get("/api/taxi-ranks/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taxiRankService, times(1)).getRankById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldCreateRank() throws Exception {
        when(taxiRankService.createRank(any(TaxiRank.class))).thenReturn(testRank1);

        mockMvc.perform(post("/api/taxi-ranks")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRankDTO1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Rank 1"))
                .andExpect(jsonPath("$.data.code").value("TEST001"));

        verify(taxiRankService, times(1)).createRank(any(TaxiRank.class));
        verify(rankMapper, times(1)).createEntityFromDTO(any(TaxiRankDTO.class));
        verify(rankMapper, times(1)).toDTO(any(TaxiRank.class), anyBoolean(), anyBoolean());
    }

    @Test
    public void shouldNotAllowUnauthenticatedCreateRank() throws Exception {
        mockMvc.perform(post("/api/taxi-ranks")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRankDTO1)))
                .andExpect(status().isUnauthorized());

        verify(taxiRankService, never()).createRank(any(TaxiRank.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void shouldUpdateRank() throws Exception {
        when(taxiRankService.getRankById(1L)).thenReturn(testRank1);
        when(taxiRankService.updateRank(eq(1L), any(TaxiRank.class))).thenReturn(testRank1);

        mockMvc.perform(put("/api/taxi-ranks/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRankDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Rank 1"));

        verify(taxiRankService, times(1)).updateRank(eq(1L), any(TaxiRank.class));
        verify(rankMapper, times(1)).createEntityFromDTO(any(TaxiRankDTO.class));
        verify(rankMapper, times(1)).toDTO(any(TaxiRank.class), anyBoolean(), anyBoolean());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void shouldDeleteRank() throws Exception {
        doNothing().when(taxiRankService).deleteRank(1L);

        mockMvc.perform(delete("/api/taxi-ranks/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank deleted successfully"));

        verify(taxiRankService, times(1)).deleteRank(1L);
    }
} 