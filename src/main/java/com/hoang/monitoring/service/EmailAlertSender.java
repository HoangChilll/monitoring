package com.hoang.monitoring.service;

import com.hoang.monitoring.entity.CheckStatus;
import com.hoang.monitoring.event.WebsiteStatusChangedEvent;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAlertSender {
    private final JavaMailSender mailSender;

    @Value("${app.alert.from}")
    private String from;

    public void send(String to, WebsiteStatusChangedEvent event) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(buildSubject(event));
            msg.setText(buildBody(event));
            mailSender.send(msg);
            log.info("Email alert sent to {} for website {}", to, event.websiteId());
        } catch (Exception e) {
            log.error("Failed to send email alert to {}: {}", to, e.getMessage());
            // Không throw — alert fail không nên crash listener
        }
    }

    private String buildSubject(WebsiteStatusChangedEvent e) {
        String icon = e.currentStatus() == CheckStatus.DOWN ? "🔴" : "🟢";
        return "%s [%s] %s is %s".formatted(icon, e.currentStatus(), e.websiteName(), e.currentStatus());
    }

    private String buildBody(WebsiteStatusChangedEvent e) {
        return """
                Website: %s
                URL: %s
                Status: %s → %s
                Time: %s
                %s
                %s
                """.formatted(
                e.websiteName(),
                e.websiteUrl(),
                e.previousStatus(), e.currentStatus(),
                e.changedAt(),
                e.responseTimeMs() != null ? "Response time: " + e.responseTimeMs() + "ms" : "",
                e.errorMessage() != null ? "Error: " + e.errorMessage() : ""
        );
    }
}