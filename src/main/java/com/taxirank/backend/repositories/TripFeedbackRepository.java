package com.taxirank.backend.repositories;

import com.taxirank.backend.models.TripFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripFeedbackRepository extends JpaRepository<TripFeedback, Long> {
    boolean existsByTripId(Long tripId);

    List<TripFeedback> findByTerminalIdAndCreatedAtAfterAndFarePaidIsNotNull(Long terminalId, LocalDateTime after);
}
