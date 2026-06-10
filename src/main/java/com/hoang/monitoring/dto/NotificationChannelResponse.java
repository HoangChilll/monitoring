package com.hoang.monitoring.dto;

import com.hoang.monitoring.entity.ChannelType;

import java.time.Instant;

public record NotificationChannelResponse(
        Long id, ChannelType type, String target, boolean enabled, Instant createdAt
) {}