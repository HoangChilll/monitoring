package com.hoang.monitoring.repository;

import com.hoang.monitoring.entity.Website;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebsiteRepository extends JpaRepository<Website, Long> {
    List<Website> findByUserId(Long userId);
    List<Website> findByEnabledTrue();    // Lấy site cần check (Phase 5)
}