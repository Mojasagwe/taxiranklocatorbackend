package com.taxirank.backend.repositories;

import com.taxirank.backend.enums.TripStatus;
import com.taxirank.backend.models.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByUserIdOrderByTripDateDescIdDesc(Long userId);

    List<Trip> findByUserIdAndStatusInOrderByTripDateDescIdDesc(Long userId, java.util.Collection<TripStatus> statuses);

    List<Trip> findByUserIdAndStatusOrderByTripDateDescIdDesc(Long userId, TripStatus status);

    Optional<Trip> findByIdAndUserId(Long id, Long userId);

    Optional<Trip> findFirstByUserIdAndStatus(Long userId, TripStatus status);

    /** Trips on a route where the rider tapped both "in the taxi" and "taxi left" */
    List<Trip> findByTerminalIdAndBoardedAtIsNotNullAndDepartedAtAfter(Long terminalId, LocalDateTime after);

    long countByUserIdAndStatus(Long userId, TripStatus status);

    @Query("SELECT COALESCE(SUM(t.fare), 0) FROM Trip t WHERE t.user.id = :userId AND t.status = :status "
            + "AND t.tripDate >= :from AND t.tripDate < :to")
    BigDecimal sumFare(@Param("userId") Long userId, @Param("status") TripStatus status,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT t.rank.id) FROM Trip t WHERE t.user.id = :userId AND t.status = :status")
    long countRanks(@Param("userId") Long userId, @Param("status") TripStatus status);

    /** Keeps the trips as anonymous counts when a rider deletes their account */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Trip t SET t.user = null WHERE t.user.id = :userId")
    int detachFromUser(@Param("userId") Long userId);
}
