package com.hoang.monitoring.service;

import com.hoang.monitoring.entity.CheckLog;
import com.hoang.monitoring.entity.CheckStatus;
import com.hoang.monitoring.entity.Website;
import com.hoang.monitoring.event.WebsiteStatusChangedEvent;
import com.hoang.monitoring.repository.CheckLogRepository;
import com.hoang.monitoring.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebsiteCheckerService {

    private final WebsiteRepository websiteRepository;
    private final CheckLogRepository checkLogRepository;
    private final HttpClient httpClient;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;   // ← thêm

    public CheckLog check(Website website) {
        CheckStatus previousStatus = website.getLastStatus();  // ← capture TRƯỚC khi override

        Instant start = Instant.now();
        CheckLog logEntry = doCheck(website, start);

        website.setLastStatus(logEntry.getStatus());
        website.setLastCheckedAt(start);
        websiteRepository.save(website);
        evictCaches(website);

        CheckLog saved = checkLogRepository.save(logEntry);

        publishIfTransition(website, previousStatus, saved);   // ← publish event

        return saved;
    }

    private void publishIfTransition(Website website, CheckStatus previousStatus, CheckLog logEntry) {
        CheckStatus currentStatus = logEntry.getStatus();
        boolean isTransition = previousStatus != null
                && previousStatus != CheckStatus.UNKNOWN
                && previousStatus != currentStatus;

        if (!isTransition) return;

        eventPublisher.publishEvent(new WebsiteStatusChangedEvent(
                website.getId(),
                website.getUser().getId(),
                website.getName(),
                website.getUrl(),
                previousStatus,
                currentStatus,
                Instant.now(),
                logEntry.getErrorMessage(),
                logEntry.getResponseTimeMs()
        ));

        log.info("[{}] status transition: {} -> {}, event published",
                website.getName(), previousStatus, currentStatus);
    }

    private CheckLog doCheck(Website website, Instant start) {
        CheckLog.CheckLogBuilder builder = CheckLog.builder()
                .website(website)
                .checkedAt(start);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(website.getUrl()))
                    .timeout(Duration.ofSeconds(website.getTimeoutSeconds()))
                    .GET()
                    .build();

            long t0 = System.currentTimeMillis();
            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding());
            long elapsed = System.currentTimeMillis() - t0;

            CheckStatus status = response.statusCode() == website.getExpectedStatusCode()
                    ? CheckStatus.UP
                    : CheckStatus.DOWN;

            log.info("[{}] {} -> {} ({}ms)", website.getName(), website.getUrl(),
                    response.statusCode(), elapsed);

            return builder
                    .status(status)
                    .statusCode(response.statusCode())
                    .responseTimeMs(elapsed)
                    .build();

        } catch (Exception e) {
            long elapsed = Duration.between(start, Instant.now()).toMillis();
            String err = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("[{}] {} -> ERROR: {}", website.getName(), website.getUrl(), err);

            return builder
                    .status(CheckStatus.DOWN)
                    .responseTimeMs(elapsed)
                    .errorMessage(truncate(err, 500))
                    .build();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private void evictCaches(Website website) {
        var websiteCache = cacheManager.getCache("website");
        if (websiteCache != null) websiteCache.evict(website.getId());

        var listCache = cacheManager.getCache("websites");
        if (listCache != null) listCache.evict(website.getUser().getId());
    }
}