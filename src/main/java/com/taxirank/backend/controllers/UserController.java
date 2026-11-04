package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.MyAccountDtos.DeleteAccountRequest;
import com.taxirank.backend.dto.MyAccountDtos.NotificationSettings;
import com.taxirank.backend.dto.MyAccountDtos.UpdateMyDetailsRequest;
import com.taxirank.backend.dto.UserResponse;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.AccountAdminService;
import com.taxirank.backend.services.MyAccountService;
import com.taxirank.backend.services.ProfilePhotoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * The signed-in user's own account, plus profile photos. Managing other accounts is /api/admin/users.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    @Autowired
    private AccountAdminService accountAdminService;

    @Autowired
    private MyAccountService myAccountService;

    @Autowired
    private ProfilePhotoService profilePhotoService;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Current user",
                UserResponse.from(accountAdminService.getUser(principal.getId()))));
    }

    @PutMapping("/me")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMyDetailsRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Details saved",
                UserResponse.from(myAccountService.updateDetails(principal.getId(), request))));
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<?> notifications(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Notification settings",
                myAccountService.notifications(principal.getId())));
    }

    @PutMapping("/me/notifications")
    public ResponseEntity<?> updateNotifications(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationSettings settings) {
        return ResponseEntity.ok(ApiResponse.success("Notification settings saved",
                myAccountService.updateNotifications(principal.getId(), settings)));
    }

    /** POST rather than DELETE: it needs a body (password and confirmation) */
    @PostMapping("/me/delete")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeleteAccountRequest request) {
        myAccountService.deleteAccount(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Your account has been deleted", null));
    }

    /** multipart/form-data with the image in "photo" */
    @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("photo") MultipartFile photo) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Photo saved",
                UserResponse.from(profilePhotoService.upload(principal.getId(), photo.getBytes()))));
    }

    @DeleteMapping("/me/photo")
    public ResponseEntity<?> removePhoto(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Photo removed",
                UserResponse.from(profilePhotoService.remove(principal.getId()))));
    }

    /** Your own photo, or anyone's for those who can view users */
    @GetMapping("/{id}/photo")
    @PreAuthorize("#id == authentication.principal.id or hasAuthority('USERS_VIEW')")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        return profilePhotoService.load(id)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        // URLs change with every new photo (?v=), so the app can cache them; private keeps proxies out
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePrivate())
                        .body(bytes))
                .orElse(ResponseEntity.notFound().build());
    }
}
