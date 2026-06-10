package com.hoang.monitoring.event;

import com.hoang.monitoring.entity.CheckStatus;

import java.time.Instant;

public record WebsiteStatusChangedEvent(
        Long websiteId,
        Long userId,
        String websiteName,
        String websiteUrl,
        CheckStatus previousStatus,
        CheckStatus currentStatus,
        Instant changedAt,
        String errorMessage,        // nullable, có khi DOWN
        Long responseTimeMs         // nullable
) {}