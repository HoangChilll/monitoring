package com.hoang.monitoring.service;

import com.hoang.monitoring.dto.CheckLogResponse;
import com.hoang.monitoring.dto.WebsiteRequest;
import com.hoang.monitoring.dto.WebsiteResponse;
import com.hoang.monitoring.entity.User;
import com.hoang.monitoring.entity.Website;
import com.hoang.monitoring.exception.BadRequestException;
import com.hoang.monitoring.exception.ResourceNotFoundException;
import com.hoang.monitoring.repository.CheckLogRepository;
import com.hoang.monitoring.repository.WebsiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final CheckLogRepository checkLogRepository;
    private final CurrentUserService currentUserService;

    // ============================================================
    //  READ
    // ============================================================

    @Transactional(readOnly = true)
    @Cacheable(value = "websites", key = "#root.target.currentUserId()")
    public List<WebsiteResponse> listMine() {
        User currentUser = currentUserService.getCurrentUser();
        return websiteRepository.findByUserId(currentUser.getId()).stream()
                .map(WebsiteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "website", key = "#id")
    public WebsiteResponse getById(Long id) {
        return WebsiteResponse.from(findOwned(id));
    }

    @Transactional(readOnly = true)
    public Page<CheckLogResponse> getLogs(Long id, int page, int size) {
        findOwned(id);
        if (size > 100) throw new BadRequestException("Page size must be <= 100");

        return checkLogRepository
                .findByWebsiteIdOrderByCheckedAtDesc(id,
                        PageRequest.of(page, size, Sort.by("checkedAt").descending()))
                .map(CheckLogResponse::from);
    }

    // ============================================================
    //  WRITE — invalidate cache
    // ============================================================

    @CacheEvict(value = "websites", key = "#root.target.currentUserId()")
    public WebsiteResponse create(WebsiteRequest req) {
        User currentUser = currentUserService.getCurrentUser();

        Website website = Website.builder()
                .name(req.getName())
                .url(req.getUrl())
                .checkIntervalSeconds(req.getCheckIntervalSeconds())
                .timeoutSeconds(req.getTimeoutSeconds())
                .expectedStatusCode(req.getExpectedStatusCode())
                .enabled(req.getEnabled() != null ? req.getEnabled() : true)
                .user(currentUser)
                .build();

        return WebsiteResponse.from(websiteRepository.save(website));
    }

    @Caching(evict = {
            @CacheEvict(value = "website",  key = "#id"),
            @CacheEvict(value = "websites", key = "#root.target.currentUserId()")
    })
    public WebsiteResponse update(Long id, WebsiteRequest req) {
        Website website = findOwned(id);

        website.setName(req.getName());
        website.setUrl(req.getUrl());
        website.setCheckIntervalSeconds(req.getCheckIntervalSeconds());
        website.setTimeoutSeconds(req.getTimeoutSeconds());
        website.setExpectedStatusCode(req.getExpectedStatusCode());
        if (req.getEnabled() != null) website.setEnabled(req.getEnabled());

        return WebsiteResponse.from(websiteRepository.save(website));
    }

    @Caching(evict = {
            @CacheEvict(value = "website",  key = "#id"),
            @CacheEvict(value = "websites", key = "#root.target.currentUserId()")
    })
    public void delete(Long id) {
        Website website = findOwned(id);
        websiteRepository.delete(website);
    }

    // ============================================================
    //  Helpers
    // ============================================================

    /** Public để dùng được trong SpEL của @Cacheable/@CacheEvict */
    public Long currentUserId() {
        return currentUserService.getCurrentUser().getId();
    }

    private Website findOwned(Long id) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found: " + id));

        User currentUser = currentUserService.getCurrentUser();
        if (!website.getUser().getId().equals(currentUser.getId())) {
            // Trả 404 thay vì 403 để không leak info
            throw new ResourceNotFoundException("Website not found: " + id);
        }
        return website;
    }
}