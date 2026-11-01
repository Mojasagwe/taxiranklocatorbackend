package com.taxirank.backend.services;

import com.taxirank.backend.dto.ApplicationDtos.ApplicationRequest;
import com.taxirank.backend.dto.ApplicationDtos.ApprovalResponse;
import com.taxirank.backend.dto.ApplicationDtos.ApproveRequest;
import com.taxirank.backend.dto.ApplicationDtos.NearbyRank;
import com.taxirank.backend.models.RankApplication;

import java.util.List;

/**
 * Applications to manage a rank. One open application per applicant; a rank with
 * an account can't be applied for or approved again.
 */
public interface RankApplicationService {
    RankApplication submit(Long applicantId, ApplicationRequest request);

    List<RankApplication> findByApplicant(Long applicantId);

    /** Applicant edits an open application; it goes back to PENDING */
    RankApplication resubmit(Long id, Long applicantId, ApplicationRequest request);

    RankApplication withdraw(Long id, Long applicantId);

    /** filter: "open" (default), "decided" or "all" */
    List<RankApplication> list(String filter);

    RankApplication get(Long id);

    /** Existing ranks within 1 km of a proposed rank's pin, nearest first */
    List<NearbyRank> nearbyRanks(RankApplication application);

    RankApplication requestInfo(Long id, String note, Long reviewerId);

    RankApplication reject(Long id, String reason, Long reviewerId);

    /** Point a proposal at a rank that already exists */
    RankApplication linkToRank(Long id, Long rankId, Long reviewerId);

    /** Creates the rank for proposals, then the rank account; returns its one-time login */
    ApprovalResponse approve(Long id, ApproveRequest request, Long reviewerId);
}
