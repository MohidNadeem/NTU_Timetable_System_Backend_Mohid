package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.NotificationDto;
import com.ntu.timetabling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// available to any authenticated user (Lecturer or Timetabling Team) - each just sees their own
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> getMine(Authentication authentication) {
        return notificationService.getForUser(authentication.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        return Map.of("count", notificationService.getUnreadCount(authentication.getName()));
    }

    @PutMapping("/{id}/read")
    public void markRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markRead(id, authentication.getName());
    }

    @PutMapping("/read-all")
    public void markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
    }
}
