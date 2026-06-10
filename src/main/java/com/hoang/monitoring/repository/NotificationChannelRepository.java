package com.hoang.monitoring.repository;

import com.hoang.monitoring.entity.ChannelType;
import com.hoang.monitoring.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    List<NotificationChannel> findByUserIdAndEnabledTrue(Long userId);
    Optional<NotificationChannel> findByUserIdAndType(Long userId, ChannelType type);
}