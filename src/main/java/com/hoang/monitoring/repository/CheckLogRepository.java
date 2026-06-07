package com.hoang.monitoring.repository;
import com.hoang.monitoring.entity.CheckLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckLogRepository extends JpaRepository<CheckLog, Long> {
    Page<CheckLog> findByWebsiteIdOrderByCheckedAtDesc(Long websiteId, Pageable pageable);
}