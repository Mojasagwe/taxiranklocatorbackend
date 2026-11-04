package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.exceptions.ConflictException;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.AccountEvent;
import com.taxirank.backend.models.Profile;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.AccountEventRepository;
import com.taxirank.backend.repositories.ProfileRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    // Rank passwords are shared by a team and live longer, so they must be longer
    static final int RANK_ACCOUNT_MIN_PASSWORD = 12;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AccountEventRepository accountEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public User registerCommuter(RegisterRequest request) {
        return createPerson(request, Profile.COMMUTER);
    }

    @Override
    @Transactional
    public User login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLoginName(), loginRequest.getPassword()));
        } catch (LockedException e) {
            // Spring checks the lock before the password; only reveal a suspension to someone who knows the password
            boolean passwordMatches = findByLoginName(loginRequest.getLoginName())
                    .map(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
                    .orElse(false);
            throw passwordMatches ? e : new BadCredentialsException("Bad credentials");
        }
        User user = findByLoginName(loginRequest.getLoginName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User bootstrapSystemAdmin(RegisterRequest request) {
        if (userRepository.existsByProfileCode(Profile.SYSTEM_ADMIN)) {
            throw new ConflictException("A System Administrator already exists");
        }
        User admin = createPerson(request, Profile.SYSTEM_ADMIN);
        accountEventRepository.save(new AccountEvent(admin, null, AccountEvent.ACCOUNT_CREATED, "First System Administrator (bootstrap)"));
        return admin;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from the current one");
        }
        if (user.isRankAccount() && newPassword.length() < RANK_ACCOUNT_MIN_PASSWORD) {
            throw new IllegalArgumentException("Rank account passwords need at least " + RANK_ACCOUNT_MIN_PASSWORD + " characters");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        accountEventRepository.save(new AccountEvent(user, user, AccountEvent.PASSWORD_CHANGED, null));
    }

    private User createPerson(RegisterRequest request, String profileCode) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("An account with this phone number already exists");
        }
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfile(profile(profileCode));
        return userRepository.save(user);
    }

    private Optional<User> findByLoginName(String loginName) {
        return userRepository.findByEmailIgnoreCase(loginName).or(() -> userRepository.findByUsernameIgnoreCase(loginName));
    }

    private Profile profile(String code) {
        return profileRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Profile " + code + " is missing; check the Flyway migrations"));
    }
}
