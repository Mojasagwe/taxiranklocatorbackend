package com.taxirank.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.auto-create:false}")
    private boolean autoCreateAdmin;
    
    @Value("${app.admin.email:admin@taxirank.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Override
    public void run(String... args) {
        // Only create admin if auto-create is enabled
        if (autoCreateAdmin) {
            // Check if admin exists
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setPhoneNumber("0000000000"); // placeholder
                admin.setAccountStatus(AccountStatus.ACTIVE);
                admin.setRole(UserRole.ADMIN);
                
                userRepository.save(admin);
                
                System.out.println("Default admin user created: " + adminEmail);
                System.out.println("IMPORTANT: Please change the default admin password!");
            }
        }
    }
} 