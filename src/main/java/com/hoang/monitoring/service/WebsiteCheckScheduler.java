package com.hoang.monitoring.service;

import com.hoang.monitoring.entity.Website;
import com.hoang.monitoring.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsiteCheckScheduler {

    private final WebsiteRepository websiteRepository;
    private final WebsiteCheckerService checkerService;

    // Chạy mỗi 30s, lần chạy mới bắt đầu sau khi lần trước xong + 30s
    @Scheduled(fixedDelay = 30_000)
    public void checkDueWebsites() {
        List<Website> due = websiteRepository.findAllByEnabledTrue().stream()
                .filter(this::isDueForCheck)
                .toList();

        if (due.isEmpty()) {
            log.debug("No websites due for check");
            return;
        }

        log.info("Checking {} websites", due.size());

        // parallelStream để không bị 1 website chậm chặn cả batch
        due.parallelStream().forEach(w -> {
            try {
                checkerService.check(w);
            } catch (Exception e) {
                log.error("Failed to check website {}: {}", w.getId(), e.getMessage());
            }
        });
    }

    private boolean isDueForCheck(Website w) {
        if (w.getLastCheckedAt() == null) return true;
        Instant nextDue = w.getLastCheckedAt().plusSeconds(w.getCheckIntervalSeconds());
        return !nextDue.isAfter(Instant.now());
    }
}