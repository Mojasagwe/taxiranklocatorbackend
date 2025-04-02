package com.taxirank.backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taxirank.backend.enums.UserRole;
import com.taxirank.backend.models.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.managedRanks WHERE u.id = :id")
    Optional<User> findByIdWithManagedRanks(@Param("id") Long id);
} 