package com.hoang.monitoring.dto;

import com.hoang.monitoring.entity.CheckLog;
import com.hoang.monitoring.entity.CheckStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CheckLogResponse {
    private Long id;
    private CheckStatus status;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
    private Instant checkedAt;

    public static CheckLogResponse from(CheckLog log) {
        return CheckLogResponse.builder()
                .id(log.getId())
                .status(log.getStatus())
                .statusCode(log.getStatusCode())
                .responseTimeMs(log.getResponseTimeMs())
                .errorMessage(log.getErrorMessage())
                .checkedAt(log.getCheckedAt())
                .build();
    }
}