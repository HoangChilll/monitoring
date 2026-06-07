package com.hoang.monitoring.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "check_logs", indexes = {
        @Index(name = "idx_checklog_website", columnList = "website_id"),
        @Index(name = "idx_checklog_checked_at", columnList = "checked_at")// đánh index cho cột
})
@Getter
@Setter
@NoArgsConstructor // tự sinh constructor không tham số
@AllArgsConstructor // sinh constructor tham số
@Builder
public class CheckLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "website_id", nullable = false)
    private Website website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckStatus status;

    private Integer statusCode;             // HTTP status code (nullable nếu lỗi network)

    @Column(nullable = false)
    private Long responseTimeMs;            // Thời gian phản hồi (ms)

    @Column(length = 1000) // độ dài tối đa của chuỗi string
    private String errorMessage;            // Nullable, chỉ có khi DOWN

    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt;

    @PrePersist // thêm thời gian khi thêm dữ liệu
    void onCreate() {
        this.checkedAt = Instant.now();
    }
}