package com.taxirank.backend.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class HealthConfig {

    /**
     * Custom health indicator to provide more detailed health information
     * and ensure the health check endpoint returns valid data for the Heroku deployment.
     */
    @Bean
    public HealthIndicator applicationHealthIndicator(JdbcTemplate jdbcTemplate) {
        return () -> {
            try {
                // Test database connection
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                
                // Add additional application-specific health checks as needed
                
                return Health.up()
                        .withDetail("applicationStatus", "Running")
                        .withDetail("description", "Taxi Rank Backend is operational")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("applicationStatus", "Not available")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        };
    }
} 