package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.LecturerDashboardDto;
import com.ntu.timetabling.service.LecturerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Increment 1: name, teaching load, and request
 * status summary
 *
 * Timetabling Team side has still the placeholder
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final LecturerDashboardService lecturerDashboardService;

    @GetMapping("/api/lecturer/dashboard")
    public ResponseEntity<LecturerDashboardDto> lecturerDashboard(Authentication authentication) {
        return ResponseEntity.ok(lecturerDashboardService.getDashboard(authentication.getName()));
    }

    @GetMapping("/api/timetabling-team/dashboard")
    public ResponseEntity<?> timetablingTeamDashboard(Authentication authentication) {
        log.info("Timetabling Team dashboard hit by user: {}", authentication.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Timetabling Team dashboard placeholder - logged in as " + authentication.getName()
        ));
    }
}
