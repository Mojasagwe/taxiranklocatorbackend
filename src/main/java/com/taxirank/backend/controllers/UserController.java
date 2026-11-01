package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.AccountAdminDtos.SelfUpdateRequest;
import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.UserResponse;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.AccountAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * The signed-in user's own account. Managing other accounts is /api/admin/users.
 */
@RestController
@RequestMapping("/api/users/me")
@PreAuthorize("isAuthenticated()")
public class UserController {

    @Autowired
    private AccountAdminService accountAdminService;

    @GetMapping
    public ResponseEntity<?> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Current user",
                UserResponse.from(accountAdminService.getUser(principal.getId()))));
    }

    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SelfUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                UserResponse.from(accountAdminService.updateSelf(principal.getId(), request))));
    }
}
