package com.hoang.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "alertExecutor")
    public Executor alertExecutor() {
        // Đã bật virtual threads global rồi, nên đơn giản:
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
