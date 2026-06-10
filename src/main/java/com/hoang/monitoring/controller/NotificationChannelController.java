package com.hoang.monitoring.controller;

import com.hoang.monitoring.dto.NotificationChannelRequest;
import com.hoang.monitoring.dto.NotificationChannelResponse;
import com.hoang.monitoring.service.NotificationChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationChannelController {
    private final NotificationChannelService service;

    @GetMapping
    public List<NotificationChannelResponse> list() {
        return service.listMine();
    }

    @PostMapping
    public NotificationChannelResponse upsert(@Valid @RequestBody NotificationChannelRequest req) {
        return service.upsertMine(req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteMine(id);
    }
}