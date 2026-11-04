package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.AccountAdminDtos.CreateUserRequest;
import com.taxirank.backend.dto.AccountAdminDtos.EventResponse;
import com.taxirank.backend.dto.AccountAdminDtos.ResponsiblePersonRequest;
import com.taxirank.backend.dto.AccountAdminDtos.StatusRequest;
import com.taxirank.backend.dto.AccountAdminDtos.UpdateUserRequest;
import com.taxirank.backend.dto.AccountAdminDtos.UserDetailResponse;
import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.UserResponse;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.AccountAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * System Administrator account management: users, profiles, rank accounts,
 * password resets and account status.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AccountAdminService accountAdminService;

    @GetMapping
    @PreAuthorize("hasAuthority('USERS_VIEW')")
    public ResponseEntity<?> list(@RequestParam(required = false) String profile,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.success("Users",
                accountAdminService.search(profile, status, q).stream().map(UserResponse::from).toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS_VIEW')")
    public ResponseEntity<?> get(@PathVariable Long id) {
        UserDetailResponse detail = new UserDetailResponse(
                UserResponse.from(accountAdminService.getUser(id)),
                accountAdminService.history(id).stream().map(EventResponse::from).toList());
        return ResponseEntity.ok(ApiResponse.success("User", detail));
    }

    /** Returns the temporary password once */
    @PostMapping
    @PreAuthorize("hasAuthority('USERS_MANAGE')")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created", accountAdminService.createUser(request, principal.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USERS_MANAGE')")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Account updated",
                UserResponse.from(accountAdminService.updateUser(id, request, principal.getId()))));
    }

    @PutMapping("/{id}/responsible-person")
    @PreAuthorize("hasAuthority('USERS_MANAGE')")
    public ResponseEntity<?> setResponsiblePerson(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody ResponsiblePersonRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Responsible person updated",
                UserResponse.from(accountAdminService.setResponsiblePerson(id, request, principal.getId()))));
    }

    /** Returns the temporary password once */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USERS_RESET_PASSWORD')")
    public ResponseEntity<?> resetPassword(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Password reset",
                accountAdminService.resetPassword(id, principal.getId())));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USERS_SET_STATUS')")
    public ResponseEntity<?> setStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                UserResponse.from(accountAdminService.setStatus(id, request, principal.getId()))));
    }
}
