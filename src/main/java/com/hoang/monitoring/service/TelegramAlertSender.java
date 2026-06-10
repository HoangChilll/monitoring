package com.hoang.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoang.monitoring.entity.CheckStatus;
import com.hoang.monitoring.event.WebsiteStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAlertSender {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${app.alert.telegram.bot-token}")
    private String botToken;

    @Value("${app.alert.telegram.api-base}")
    private String apiBase;

    public void send(String chatId, WebsiteStatusChangedEvent event) {
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram bot token not configured, skipping alert");
            return;
        }
        try {
            String url = "%s/bot%s/sendMessage".formatted(apiBase, botToken);
            Map<String, Object> payload = Map.of(
                    "chat_id", chatId,
                    "text", buildMessage(event),
                    "parse_mode", "Markdown"
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.error("Telegram returned {}: {}", resp.statusCode(), resp.body());
            } else {
                log.info("Telegram alert sent to chat {} for website {}", chatId, event.websiteId());
            }
        } catch (Exception e) {
            log.error("Failed to send telegram alert to {}: {}", chatId, e.getMessage());
        }
    }

    private String buildMessage(WebsiteStatusChangedEvent e) {
        String icon = e.currentStatus() == CheckStatus.DOWN ? "🔴" : "🟢";
        return """
                %s *%s* is now *%s*

                URL: %s
                Previous: `%s` → New: `%s`
                Time: %s
                %s
                """.formatted(
                icon, escapeMd(e.websiteName()), e.currentStatus(),
                escapeMd(e.websiteUrl()),
                e.previousStatus(), e.currentStatus(),
                e.changedAt(),
                e.errorMessage() != null ? "Error: `" + escapeMd(e.errorMessage()) + "`" : ""
        );
    }

    private String escapeMd(String s) {
        return s == null ? "" : s.replace("_", "\\_").replace("*", "\\*").replace("`", "\\`");
    }
}