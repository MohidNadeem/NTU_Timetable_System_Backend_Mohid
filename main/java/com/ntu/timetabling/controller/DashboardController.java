package com.ntu.timetabling.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Placeholder dashboard endpoints. The goal right now is just proving the
 * login -> JWT -> role-gated route pipeline works end to end; these get
 * replaced/extended as Increment 1 (and later increments) are built out on
 * top of them.
 */
@Slf4j
@RestController
public class DashboardController {

    @GetMapping("/api/lecturer/dashboard")
    public ResponseEntity<?> lecturerDashboard(Authentication authentication) {
        log.info("Lecturer dashboard hit by user: {}", authentication.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Lecturer dashboard placeholder - logged in as " + authentication.getName()
        ));
    }

    @GetMapping("/api/timetabling-team/dashboard")
    public ResponseEntity<?> timetablingTeamDashboard(Authentication authentication) {
        log.info("Timetabling Team dashboard hit by user: {}", authentication.getName());
        return ResponseEntity.ok(Map.of(
                "message", "Timetabling Team dashboard placeholder - logged in as " + authentication.getName()
        ));
    }
}
