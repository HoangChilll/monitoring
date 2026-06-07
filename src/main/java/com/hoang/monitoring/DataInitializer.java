package com.hoang.monitoring;

import com.hoang.monitoring.entity.User;
import com.hoang.monitoring.entity.Website;
import com.hoang.monitoring.repository.UserRepository;
import com.hoang.monitoring.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")   // Chỉ chạy khi profile = dev
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebsiteRepository websiteRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;   // Đã có data thì skip

        User user = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))    // Phase 4 sẽ thay bằng BCrypt
                .name("Test User")
                .build();
        user = userRepository.save(user);

        websiteRepository.save(Website.builder()
                .name("Google")
                .url("https://www.google.com")
                .user(user)
                .build());

        websiteRepository.save(Website.builder()
                .name("GitHub")
                .url("https://github.com")
                .user(user)
                .build());

        System.out.println("✅ Sample data created");
    }
}