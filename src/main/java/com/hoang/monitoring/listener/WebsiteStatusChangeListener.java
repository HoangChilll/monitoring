package com.hoang.monitoring.listener;

import com.hoang.monitoring.entity.NotificationChannel;
import com.hoang.monitoring.event.WebsiteStatusChangedEvent;
import com.hoang.monitoring.repository.NotificationChannelRepository;
import com.hoang.monitoring.service.EmailAlertSender;
import com.hoang.monitoring.service.TelegramAlertSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsiteStatusChangeListener {
    private final NotificationChannelRepository channelRepository;
    private final EmailAlertSender emailSender;
    private final TelegramAlertSender telegramSender;

    @Async("alertExecutor")
    @EventListener
    public void handle(WebsiteStatusChangedEvent event) {
        log.info("Status change: website={} {} → {}",
                event.websiteId(), event.previousStatus(), event.currentStatus());

        List<NotificationChannel> channels =
                channelRepository.findByUserIdAndEnabledTrue(event.userId());

        for (NotificationChannel ch : channels) {
            switch (ch.getType()) {
                case EMAIL -> emailSender.send(ch.getTarget(), event);
                case TELEGRAM -> telegramSender.send(ch.getTarget(), event);
            }
            ch.setLastAlertSentAt(Instant.now());
        }
        channelRepository.saveAll(channels);
    }
}