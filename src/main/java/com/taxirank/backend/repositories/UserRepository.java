package com.taxirank.backend.repositories;

import com.taxirank.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByRankId(Long rankId);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByRankId(Long rankId);
    boolean existsByProfileCode(String profileCode);
    List<User> findAllByOrderByCreatedAtDesc();
}
