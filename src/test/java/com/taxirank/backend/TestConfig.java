package com.taxirank.backend;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taxirank.backend.repositories.AdminRegistrationRepository;
import com.taxirank.backend.repositories.RankAdminRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.services.AdminRegistrationService;
import com.taxirank.backend.services.RankAdminService;
import com.taxirank.backend.services.TaxiRankService;

/**
 * Test configuration that provides mock beans for our tests
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
    
    @Bean
    @Primary
    public AdminRegistrationRepository adminRegistrationRepository() {
        return Mockito.mock(AdminRegistrationRepository.class);
    }
    
    @Bean
    @Primary
    public TaxiRankRepository taxiRankRepository() {
        return Mockito.mock(TaxiRankRepository.class);
    }
    
    @Bean
    @Primary
    public RankAdminRepository rankAdminRepository() {
        return Mockito.mock(RankAdminRepository.class);
    }
    
    @Bean
    @Primary
    public TaxiRankService taxiRankService() {
        return Mockito.mock(TaxiRankService.class);
    }
    
    @Bean
    @Primary
    public AdminRegistrationService adminRegistrationService() {
        return Mockito.mock(AdminRegistrationService.class);
    }
    
    @Bean
    @Primary
    public RankAdminService rankAdminService() {
        return Mockito.mock(RankAdminService.class);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 