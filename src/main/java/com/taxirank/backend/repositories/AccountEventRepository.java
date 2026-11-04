package com.taxirank.backend.repositories;

import com.taxirank.backend.models.AccountEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {
    List<AccountEvent> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);
}
