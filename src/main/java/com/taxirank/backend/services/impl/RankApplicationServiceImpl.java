package com.taxirank.backend.services.impl;

import com.taxirank.backend.dto.AccountAdminDtos.IssuedCredentials;
import com.taxirank.backend.dto.ApplicationDtos.ApplicationRequest;
import com.taxirank.backend.dto.ApplicationDtos.ApplicationResponse;
import com.taxirank.backend.dto.ApplicationDtos.ApprovalResponse;
import com.taxirank.backend.dto.ApplicationDtos.ApproveRequest;
import com.taxirank.backend.dto.ApplicationDtos.NearbyRank;
import com.taxirank.backend.dto.ApplicationDtos.ProposedRank;
import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.exceptions.ConflictException;
import com.taxirank.backend.exceptions.NotFoundException;
import com.taxirank.backend.models.RankApplication;
import com.taxirank.backend.models.TaxiRank;
import com.taxirank.backend.models.User;
import com.taxirank.backend.repositories.RankApplicationRepository;
import com.taxirank.backend.repositories.TaxiRankRepository;
import com.taxirank.backend.repositories.UserRepository;
import com.taxirank.backend.services.AccountAdminService;
import com.taxirank.backend.services.RankApplicationService;
import com.taxirank.backend.services.TaxiRankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

@Service
public class RankApplicationServiceImpl implements RankApplicationService {

    private static final double NEARBY_METERS = 1000;
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    @Autowired
    private RankApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaxiRankRepository taxiRankRepository;

    @Autowired
    private TaxiRankService taxiRankService;

    @Autowired
    private AccountAdminService accountAdminService;

    @Override
    @Transactional
    public RankApplication submit(Long applicantId, ApplicationRequest request) {
        User applicant = userRepository.findById(applicantId).orElseThrow(() -> new NotFoundException("User not found"));
        if (applicationRepository.existsByApplicantIdAndStatusIn(applicantId, ApplicationStatus.OPEN)) {
            throw new ConflictException("You already have an open application");
        }
        RankApplication application = new RankApplication();
        application.setApplicant(applicant);
        applyRequest(application, request);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankApplication> findByApplicant(Long applicantId) {
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId);
    }

    @Override
    @Transactional
    public RankApplication resubmit(Long id, Long applicantId, ApplicationRequest request) {
        RankApplication application = ownOpenApplication(id, applicantId);
        applyRequest(application, request);
        application.setStatus(ApplicationStatus.PENDING);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public RankApplication withdraw(Long id, Long applicantId) {
        RankApplication application = ownOpenApplication(id, applicantId);
        application.setStatus(ApplicationStatus.WITHDRAWN);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankApplication> list(String filter) {
        return switch (filter == null ? "open" : filter) {
            case "open" -> applicationRepository.findByStatusInOrderByCreatedAtAsc(ApplicationStatus.OPEN);
            case "decided" -> applicationRepository.findByStatusInOrderByCreatedAtAsc(
                    EnumSet.complementOf(EnumSet.copyOf(ApplicationStatus.OPEN)));
            case "all" -> applicationRepository.findAllByOrderByCreatedAtDesc();
            default -> throw new IllegalArgumentException("status must be open, decided or all");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public RankApplication get(Long id) {
        RankApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (application.getRankAccount() != null) {
            application.getRankAccount().getLoginName(); // load the lazy account inside the transaction
        }
        return application;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NearbyRank> nearbyRanks(RankApplication application) {
        if (!application.isProposal() || application.getProposedLatitude() == null || application.getProposedLongitude() == null) {
            return List.of();
        }
        double lat = application.getProposedLatitude();
        double lon = application.getProposedLongitude();
        return taxiRankRepository.findAll().stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .map(r -> new NearbyRank(r.getId(), r.getCode(), r.getName(),
                        Math.round(distanceMeters(lat, lon, r.getLatitude(), r.getLongitude())),
                        userRepository.existsByRankId(r.getId())))
                .filter(n -> n.distanceMeters() <= NEARBY_METERS)
                .sorted(Comparator.comparingLong(NearbyRank::distanceMeters))
                .toList();
    }

    @Override
    @Transactional
    public RankApplication requestInfo(Long id, String note, Long reviewerId) {
        RankApplication application = openApplication(id);
        application.setStatus(ApplicationStatus.INFO_REQUESTED);
        markReviewed(application, note.trim(), reviewerId);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public RankApplication reject(Long id, String reason, Long reviewerId) {
        RankApplication application = openApplication(id);
        application.setStatus(ApplicationStatus.REJECTED);
        markReviewed(application, reason.trim(), reviewerId);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public RankApplication linkToRank(Long id, Long rankId, Long reviewerId) {
        RankApplication application = openApplication(id);
        application.setRank(availableRank(rankId));
        return applicationRepository.save(application);
    }

    @Override
    @Transactional
    public ApprovalResponse approve(Long id, ApproveRequest request, Long reviewerId) {
        RankApplication application = openApplication(id);
        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> new NotFoundException("User not found"));

        TaxiRank rank = application.isProposal()
                ? createProposedRank(application, request)
                : availableRank(application.getRank().getId());
        application.setRank(rank);

        User applicant = application.getApplicant();
        IssuedCredentials credentials = accountAdminService.createRankAccount(rank, applicant.getDisplayName(),
                applicant.getPhoneNumber(), reviewer, "From application #" + application.getId());

        application.setStatus(ApplicationStatus.APPROVED);
        application.setRankAccount(userRepository.getReferenceById(credentials.user().id()));
        markReviewed(application, application.getReviewerNote(), reviewerId);
        applicationRepository.save(application);

        return new ApprovalResponse(ApplicationResponse.from(application, null), credentials.loginName(),
                credentials.temporaryPassword());
    }

    private void applyRequest(RankApplication application, ApplicationRequest request) {
        if ((request.rankId() == null) == (request.proposedRank() == null)) {
            throw new IllegalArgumentException("Choose an existing rank or describe a new one, not both");
        }
        if (request.rankId() != null) {
            application.setRank(availableRank(request.rankId()));
            setProposal(application, null);
        } else {
            application.setRank(null);
            setProposal(application, request.proposedRank());
        }
        application.setDesignation(request.designation().trim());
        application.setMotivation(request.motivation() == null ? null : request.motivation().trim());
    }

    private TaxiRank createProposedRank(RankApplication application, ApproveRequest request) {
        ProposedRank details = request != null && request.rank() != null ? request.rank() : ApplicationResponse.from(application, null).proposedRank();
        String code = request != null && request.code() != null
                ? request.code()
                : taxiRankService.generateRankCode(details.name(), details.province());
        if (taxiRankRepository.existsByCode(code)) {
            throw new ConflictException("Rank code " + code + " is already used");
        }
        TaxiRank rank = new TaxiRank();
        rank.setName(details.name().trim());
        rank.setCode(code);
        rank.setAddress(details.address().trim());
        rank.setCity(details.city());
        rank.setProvince(details.province());
        rank.setLatitude(details.latitude());
        rank.setLongitude(details.longitude());
        rank.setOperatingHours(details.operatingHours());
        rank.setCapacity(details.capacity());
        rank.setContactPhone(details.contactPhone());
        rank.setContactEmail(details.contactEmail());
        rank.setIsActive(true);
        return taxiRankRepository.save(rank);
    }

    private TaxiRank availableRank(Long rankId) {
        TaxiRank rank = taxiRankRepository.findById(rankId).orElseThrow(() -> new NotFoundException("Rank not found"));
        if (userRepository.existsByRankId(rankId)) {
            throw new ConflictException(rank.getName() + " already has a rank admin");
        }
        return rank;
    }

    private RankApplication openApplication(Long id) {
        RankApplication application = get(id);
        if (!application.getStatus().isOpen()) {
            throw new ConflictException("This application is already " + application.getStatus().name().toLowerCase());
        }
        return application;
    }

    // Someone else's application looks the same as a missing one
    private RankApplication ownOpenApplication(Long id, Long applicantId) {
        RankApplication application = applicationRepository.findById(id)
                .filter(a -> a.getApplicant().getId().equals(applicantId))
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!application.getStatus().isOpen()) {
            throw new ConflictException("This application is already " + application.getStatus().name().toLowerCase());
        }
        return application;
    }

    private void markReviewed(RankApplication application, String note, Long reviewerId) {
        application.setReviewerNote(note);
        application.setReviewedBy(userRepository.getReferenceById(reviewerId));
        application.setReviewedAt(LocalDateTime.now());
    }

    private static void setProposal(RankApplication application, ProposedRank proposal) {
        application.setProposedName(proposal == null ? null : proposal.name().trim());
        application.setProposedAddress(proposal == null ? null : proposal.address().trim());
        application.setProposedCity(proposal == null ? null : proposal.city());
        application.setProposedProvince(proposal == null ? null : proposal.province());
        application.setProposedLatitude(proposal == null ? null : proposal.latitude());
        application.setProposedLongitude(proposal == null ? null : proposal.longitude());
        application.setProposedOperatingHours(proposal == null ? null : proposal.operatingHours());
        application.setProposedCapacity(proposal == null ? null : proposal.capacity());
        application.setProposedContactPhone(proposal == null ? null : proposal.contactPhone());
        application.setProposedContactEmail(proposal == null ? null : proposal.contactEmail());
    }

    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.sqrt(a));
    }
}
