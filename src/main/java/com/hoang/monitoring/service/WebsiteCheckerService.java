package com.hoang.monitoring.service;

import com.hoang.monitoring.entity.CheckLog;
import com.hoang.monitoring.entity.CheckStatus;
import com.hoang.monitoring.entity.Website;
import com.hoang.monitoring.repository.CheckLogRepository;
import com.hoang.monitoring.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public CheckLog check(Website website) {
        Instant start = Instant.now();
        CheckLog logEntry = doCheck(website, start);

        // Update website state
        website.setLastStatus(logEntry.getStatus());
        website.setLastCheckedAt(start);
        websiteRepository.save(website);

        return checkLogRepository.save(logEntry);
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
                    .responseTimeMs( elapsed)
                    .errorMessage(truncate(err, 500))
                    .build();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}