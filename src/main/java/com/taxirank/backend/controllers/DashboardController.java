package com.taxirank.backend.controllers;

import com.taxirank.backend.dto.ApiResponse;
import com.taxirank.backend.enums.AccountStatus;
import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankApplicationRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.security.UserPrincipal;
import com.taxirank.backend.utils.TaxiRankMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private RankApplicationRepository applicationRepository;

    @Autowired
    private TaxiRankMapper rankMapper;

    /** Counts for the System Administrator's home screen */
    @GetMapping("/system")
    @PreAuthorize("hasAuthority('USERS_VIEW')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> system() {
        List<User> users = userRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("usersByProfile", users.stream()
                .collect(Collectors.groupingBy(u -> u.getProfile().getCode(), Collectors.counting())));
        stats.put("accountsNeedingAttention", users.stream()
                .filter(u -> u.getAccountStatus() != AccountStatus.ACTIVE).count());
        stats.put("openApplications", applicationRepository.countByStatusIn(ApplicationStatus.OPEN));
        stats.put("ranks", taxiRankRepository.count());
        stats.put("ranksWithoutAccount", taxiRankRepository.countUnassignedRanks());
        return ResponseEntity.ok(ApiResponse.success("System dashboard", stats));
    }

    /** The rank account's own rank, with terminal counts */
    @GetMapping("/rank")
    @PreAuthorize("hasAuthority('RANK_MANAGE_OWN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> rank(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal.getRankId() == null) {
            throw new NotFoundException("This account isn't linked to a rank");
        }
        return ResponseEntity.ok(ApiResponse.success("Rank dashboard",
                rankMapper.toDTO(taxiRankRepository.findById(principal.getRankId())
                        .orElseThrow(() -> new NotFoundException("Rank not found")), true)));
    }
}
