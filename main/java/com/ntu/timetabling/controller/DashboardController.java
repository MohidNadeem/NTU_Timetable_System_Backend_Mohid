package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.LecturerDashboardDto;
import com.ntu.timetabling.dto.TimetablingTeamDashboardDto;
import com.ntu.timetabling.service.LecturerDashboardService;
import com.ntu.timetabling.service.TimetablingTeamDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Increment 1: name, teaching load, and request
 * status summary
 *
 * Timetabling Team side has still the placeholder
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final LecturerDashboardService lecturerDashboardService;
    private final TimetablingTeamDashboardService timetablingTeamDashboardService;

    @GetMapping("/api/lecturer/dashboard")
    public ResponseEntity<LecturerDashboardDto> lecturerDashboard(Authentication authentication) {
        return ResponseEntity.ok(lecturerDashboardService.getDashboard(authentication.getName()));
    }

    @GetMapping("/api/timetabling-team/dashboard")
    public ResponseEntity<TimetablingTeamDashboardDto> timetablingTeamDashboard(Authentication authentication) {
        return ResponseEntity.ok(timetablingTeamDashboardService.getDashboard(authentication.getName()));
    }
}
