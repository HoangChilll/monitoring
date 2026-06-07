package com.hoang.monitoring.controller;

import com.hoang.monitoring.dto.CheckLogResponse;
import com.hoang.monitoring.dto.WebsiteRequest;
import com.hoang.monitoring.dto.WebsiteResponse;
import com.hoang.monitoring.service.WebsiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/websites")
@RequiredArgsConstructor
public class WebsiteController {

    private final WebsiteService websiteService;

    @PostMapping
    public ResponseEntity<WebsiteResponse> create(@Valid @RequestBody WebsiteRequest req) {
        WebsiteResponse created = websiteService.create(req);
        return ResponseEntity
                .created(URI.create("/api/v1/websites/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public List<WebsiteResponse> list() {
        return websiteService.listMine();
    }

    @GetMapping("/{id}")
    public WebsiteResponse get(@PathVariable Long id) {
        return websiteService.getById(id);
    }

    @PutMapping("/{id}")
    public WebsiteResponse update(@PathVariable Long id, @Valid @RequestBody WebsiteRequest req) {
        return websiteService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        websiteService.delete(id);
    }

    @GetMapping("/{id}/logs")
    public Page<CheckLogResponse> getLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return websiteService.getLogs(id, page, size);
    }
}