package com.taxirank.backend.test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import com.taxirank.backend.services.TaxiRankService;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TerminalService;
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
    private JwtTokenProvider jwtTokenProvider;
    
    @MockBean
    private RankAdminService rankAdminService;
    
    @MockBean
    private TerminalService terminalService;
    
    @MockBean
    private TaxiRankMapper rankMapper;

    private TaxiRank testRank1;
    private TaxiRank testRank2;
    private TaxiRankDTO testRankDTO1;

    @BeforeEach
    void setUp() {
        testRank1 = new TaxiRank();
        testRank1.setId(1L);
        testRank1.setName("Test Rank 1");
        testRank1.setCode("TEST001");
        testRank1.setAddress("Test Location 1");
        testRank1.setContactPhone("+27123456789");
        testRank1.setOperatingHours("08:00-17:00");
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
        testRank2.setOperatingHours("08:00-17:00");
        testRank2.setIsActive(true);
        testRank2.setLatitude(-33.8249);
        testRank2.setLongitude(18.3241);
        testRank2.setCapacity(100);
        testRank2.setCity("Test City 2");
        testRank2.setProvince("Test Province 2");
        
        testRankDTO1 = new TaxiRankDTO();
        testRankDTO1.setId(1L);
        testRankDTO1.setName("Test Rank 1");
        testRankDTO1.setCode("TEST001");
        testRankDTO1.setAddress("Test Location 1");
        testRankDTO1.setContactPhone("+27123456789");
        testRankDTO1.setOperatingHours("08:00-17:00");
        testRankDTO1.setIsActive(true);
        testRankDTO1.setLatitude(-33.9249);
        testRankDTO1.setLongitude(18.4241);
        testRankDTO1.setCapacity(50);
        testRankDTO1.setCity("Test City 1");
        testRankDTO1.setProvince("Test Province 1");
        
        // Setup mock responses for rankMapper
        when(rankMapper.toDTOList(anyList(), anyBoolean(), anyBoolean())).thenReturn(Arrays.asList(testRankDTO1));
        when(rankMapper.toDTO(any(TaxiRank.class), anyBoolean(), anyBoolean())).thenReturn(testRankDTO1);
        when(rankMapper.createEntityFromDTO(any())).thenReturn(testRank1);
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
                .andExpect(jsonPath("$.message").value("All taxi ranks retrieved successfully"));

        verify(taxiRankService, times(1)).getAllRanks();
    }

    @Test
    @WithMockUser
    public void shouldGetRankById() throws Exception {
        when(taxiRankService.getRankById(1L)).thenReturn(testRank1);

        mockMvc.perform(get("/api/taxi-ranks/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank retrieved successfully"));

        verify(taxiRankService, times(1)).getRankById(1L);
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
        when(rankMapper.toDTO(ArgumentMatchers.eq(testRank1), anyBoolean(), anyBoolean())).thenReturn(testRankDTO1);

        mockMvc.perform(post("/api/taxi-ranks")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRankDTO1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank created successfully"));

        verify(taxiRankService, times(1)).createRank(any(TaxiRank.class));
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
        when(rankMapper.toDTO(ArgumentMatchers.eq(testRank1), anyBoolean(), anyBoolean())).thenReturn(testRankDTO1);

        mockMvc.perform(put("/api/taxi-ranks/1")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRankDTO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Taxi rank updated successfully"));

        verify(taxiRankService, times(1)).updateRank(eq(1L), any(TaxiRank.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
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