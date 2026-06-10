package com.hoang.monitoring.service;
// service/NotificationChannelService.java
import com.hoang.monitoring.dto.NotificationChannelRequest;
import com.hoang.monitoring.dto.NotificationChannelResponse;
import com.hoang.monitoring.entity.ChannelType;
import com.hoang.monitoring.entity.NotificationChannel;
import com.hoang.monitoring.entity.User;
import com.hoang.monitoring.exception.BadRequestException;
import com.hoang.monitoring.exception.ResourceNotFoundException;
import com.hoang.monitoring.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NotificationChannelService {

    private static final Pattern EMAIL_RX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEGRAM_CHAT_ID_RX =
            Pattern.compile("^-?\\d{5,20}$");  // user id dương, group id âm

    private final NotificationChannelRepository channelRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<NotificationChannelResponse> listMine() {
        User user = currentUserService.getCurrentUser();
        return channelRepository.findByUserIdAndEnabledTrue(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationChannelResponse upsertMine(NotificationChannelRequest req) {
        User user = currentUserService.getCurrentUser();
        validateTarget(req.type(), req.target());

        NotificationChannel channel = channelRepository
                .findByUserIdAndType(user.getId(), req.type())
                .orElseGet(() -> {
                    NotificationChannel c = new NotificationChannel();
                    c.setUser(user);
                    c.setType(req.type());
                    return c;
                });

        channel.setTarget(req.target().trim());
        channel.setEnabled(req.enabled() == null || req.enabled());

        return toResponse(channelRepository.save(channel));
    }

    @Transactional
    public void deleteMine(Long id) {
        User user = currentUserService.getCurrentUser();
        NotificationChannel channel = channelRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification channel not found: " + id));
        channelRepository.delete(channel);
    }

    private void validateTarget(ChannelType type, String target) {
        if (target == null || target.isBlank()) {
            throw new BadRequestException("target must not be blank");
        }
        String trimmed = target.trim();
        switch (type) {
            case EMAIL -> {
                if (!EMAIL_RX.matcher(trimmed).matches()) {
                    throw new BadRequestException("Invalid email address");
                }
            }
            case TELEGRAM -> {
                if (!TELEGRAM_CHAT_ID_RX.matcher(trimmed).matches()) {
                    throw new BadRequestException(
                            "Invalid telegram chat_id (must be numeric, 5-20 digits, optionally negative for groups)");
                }
            }
        }
    }

    private NotificationChannelResponse toResponse(NotificationChannel c) {
        return new NotificationChannelResponse(
                c.getId(),
                c.getType(),
                c.getTarget(),
                c.isEnabled(),
                c.getCreatedAt()
        );
    }
}
