package com.hoang.monitoring.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WebsiteRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Pattern(
            regexp = "^https?://.+",
            message = "URL must start with http:// or https://"
    )
    @Size(max = 500)
    private String url;

    @Min(value = 30, message = "Check interval must be at least 30 seconds")
    @Max(value = 86400, message = "Check interval must be at most 86400 seconds (1 day)")
    private Integer checkIntervalSeconds = 60;

    @Min(value = 1)
    @Max(value = 60)
    private Integer timeoutSeconds = 10;

    @Min(value = 100)
    @Max(value = 599)
    private Integer expectedStatusCode = 200;

    private Boolean enabled = true;
}