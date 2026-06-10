package com.hoang.monitoring.dto;

import com.hoang.monitoring.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationChannelRequest(
        @NotNull ChannelType type,
        @NotBlank @Size(max = 512) String target,
        Boolean enabled
) {}