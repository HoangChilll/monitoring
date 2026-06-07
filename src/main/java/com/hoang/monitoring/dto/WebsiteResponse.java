package com.hoang.monitoring.dto;

import com.hoang.monitoring.entity.CheckStatus;
import com.hoang.monitoring.entity.Website;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WebsiteResponse {
    private Long id;
    private String name;
    private String url;
    private Integer checkIntervalSeconds;
    private Integer timeoutSeconds;
    private Integer expectedStatusCode;
    private Boolean enabled;
    private CheckStatus lastStatus;
    private Instant lastCheckedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static WebsiteResponse from(Website w) {
        return WebsiteResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .url(w.getUrl())
                .checkIntervalSeconds(w.getCheckIntervalSeconds())
                .timeoutSeconds(w.getTimeoutSeconds())
                .expectedStatusCode(w.getExpectedStatusCode())
                .enabled(w.getEnabled())
                .lastStatus(w.getLastStatus())
                .lastCheckedAt(w.getLastCheckedAt())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}