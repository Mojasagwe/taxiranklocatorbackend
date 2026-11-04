package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.AuthDtos.AuthResponse;
import com.taxirank.backend.dto.AuthDtos.ChangePasswordRequest;
import com.taxirank.backend.dto.LoginRequest;
import com.taxirank.backend.dto.RegisterRequest;
import com.taxirank.backend.dto.UserResponse;
import com.taxirank.backend.models.User;
import com.taxirank.backend.security.JwtTokenProvider;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.AccountAdminService;
import com.taxirank.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountAdminService accountAdminService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // Empty (the default) disables first-System-Administrator bootstrap entirely
    @Value("${app.bootstrap.super-admin-token:}")
    private String bootstrapToken;

    /** Self sign-up for riders and rank admin applicants; both get the Commuter profile */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.registerCommuter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created", authResponse(user)));
    }

    /** "identifier" is an email for people and the rank code for rank accounts */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (!StringUtils.hasText(request.getLoginName())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Enter your email or rank code"));
        }
        try {
            User user = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse(user)));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("This account is suspended or blocked. Contact the Travy System Administrator."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Incorrect login or password"));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Current user",
                UserResponse.from(accountAdminService.getUser(principal.getId()))));
    }

    /**
     * Works while a temporary password is in force; clears the must-change flag.
     * Every older token stops working, so the response carries a new one for this phone.
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = authService.changePassword(principal.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed", authResponse(user)));
    }

    /**
     * Creates the first System Administrator. Needs X-Bootstrap-Token equal to
     * SUPER_ADMIN_BOOTSTRAP_TOKEN, so nobody can race the owner on a fresh deploy.
     * Later System Administrators are created through /api/admin/users.
     */
    @PostMapping("/bootstrap")
    public ResponseEntity<?> bootstrap(@Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Bootstrap-Token", required = false) String providedToken) {
        if (!isValidBootstrapToken(providedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("A valid bootstrap token is required"));
        }
        User admin = authService.bootstrapSystemAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("System Administrator created", authResponse(admin)));
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(UserResponse.from(user), tokenProvider.generateToken(user));
    }

    private boolean isValidBootstrapToken(String provided) {
        if (!StringUtils.hasText(bootstrapToken) || provided == null) {
            return false;
        }
        return MessageDigest.isEqual(bootstrapToken.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8));
    }
}
