package com.hoang.monitoring.service;


import com.hoang.monitoring.entity.User;
import com.hoang.monitoring.exception.BadRequestException;
import com.hoang.monitoring.exception.ResourceNotFoundException;
import com.hoang.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new BadRequestException("Chưa xác thực");
        }
        // Vì User entity implement UserDetails, principal chính là User
        if (auth.getPrincipal() instanceof User user) {
            return user;   // lưu ý: instance này có thể detached, nếu cần managed → load lại
        }
        // fallback: load theo email
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}