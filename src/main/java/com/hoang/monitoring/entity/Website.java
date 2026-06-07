package com.hoang.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "websites", indexes = {
        @Index(name = "idx_website_user", columnList = "user_id"),
        @Index(name = "idx_website_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Website {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;             // Tên hiển thị, vd "My Blog"

    @Column(nullable = false, length = 500)
    private String url;              // https://example.com

    @Column(nullable = false)
    @Builder.Default
    private Integer checkIntervalSeconds = 60;   // Check mỗi 60s

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 10;         // Timeout 10s

    @Column(nullable = false)
    @Builder.Default
    private Integer expectedStatusCode = 200;    // Mong đợi HTTP 200

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;              // Có check không

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CheckStatus lastStatus = CheckStatus.UNKNOWN;

    private Instant lastCheckedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
