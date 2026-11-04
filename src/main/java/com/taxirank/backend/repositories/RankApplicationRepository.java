package com.taxirank.backend.repositories;

import com.taxirank.backend.enums.ApplicationStatus;
import com.taxirank.backend.models.RankApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RankApplicationRepository extends JpaRepository<RankApplication, Long> {
    List<RankApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
    List<RankApplication> findByStatusInOrderByCreatedAtAsc(Collection<ApplicationStatus> statuses);
    List<RankApplication> findAllByOrderByCreatedAtDesc();
    boolean existsByApplicantIdAndStatusIn(Long applicantId, Collection<ApplicationStatus> statuses);
    long countByStatusIn(Collection<ApplicationStatus> statuses);
}
