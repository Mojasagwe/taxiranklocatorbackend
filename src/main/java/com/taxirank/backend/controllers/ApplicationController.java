package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.dto.ApplicationDtos.ApplicationRequest;
import com.taxirank.backend.dto.ApplicationDtos.ApplicationResponse;
import com.taxirank.backend.dto.ApplicationDtos.ApproveRequest;
import com.taxirank.backend.dto.ApplicationDtos.LinkRequest;
import com.taxirank.backend.dto.ApplicationDtos.NoteRequest;
import com.taxirank.backend.enums.Permission;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.RankApplication;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.services.RankApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Applications to manage a rank. Applicants (APPLICATIONS_SUBMIT) apply, follow and
 * resubmit; System Administrators (APPLICATIONS_REVIEW) decide.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private RankApplicationService applicationService;

    // --- applicant -----------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAuthority('APPLICATIONS_SUBMIT')")
    public ResponseEntity<?> submit(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplicationRequest request) {
        RankApplication application = applicationService.submit(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted", ApplicationResponse.from(application, null)));
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Your applications",
                applicationService.findByApplicant(principal.getId()).stream()
                        .map(a -> ApplicationResponse.from(a, null)).toList()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('APPLICATIONS_SUBMIT')")
    public ResponseEntity<?> resubmit(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Application resubmitted",
                ApplicationResponse.from(applicationService.resubmit(id, principal.getId(), request), null)));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn",
                ApplicationResponse.from(applicationService.withdraw(id, principal.getId()), null)));
    }

    /** The applicant sees their own; reviewers see any, plus nearby ranks for proposals */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        RankApplication application = applicationService.get(id);
        boolean reviewer = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Permission.APPLICATIONS_REVIEW.name()));
        if (!reviewer && !application.getApplicant().getId().equals(principal.getId())) {
            throw new NotFoundException("Application not found");
        }
        return ResponseEntity.ok(ApiResponse.success("Application", ApplicationResponse.from(application,
                reviewer ? applicationService.nearbyRanks(application) : null)));
    }

    // --- reviewer ------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('APPLICATIONS_REVIEW')")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "open") String status) {
        return ResponseEntity.ok(ApiResponse.success("Applications",
                applicationService.list(status).stream().map(a -> ApplicationResponse.from(a, null)).toList()));
    }

    @PostMapping("/{id}/request-info")
    @PreAuthorize("hasAuthority('APPLICATIONS_REVIEW')")
    public ResponseEntity<?> requestInfo(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success("More information requested",
                ApplicationResponse.from(applicationService.requestInfo(id, request.note(), principal.getId()), null)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('APPLICATIONS_REVIEW')")
    public ResponseEntity<?> reject(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Application rejected",
                ApplicationResponse.from(applicationService.reject(id, request.note(), principal.getId()), null)));
    }

    @PostMapping("/{id}/link")
    @PreAuthorize("hasAuthority('APPLICATIONS_REVIEW')")
    public ResponseEntity<?> link(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody LinkRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Linked to existing rank",
                ApplicationResponse.from(applicationService.linkToRank(id, request.rankId(), principal.getId()), null)));
    }

    /** Returns the new rank account's temporary password once */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPLICATIONS_REVIEW')")
    public ResponseEntity<?> approve(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id,
            @Valid @RequestBody(required = false) ApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Application approved",
                applicationService.approve(id, request, principal.getId())));
    }
}
