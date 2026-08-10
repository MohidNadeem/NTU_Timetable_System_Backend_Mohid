package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Increment 0 - read-only timetable view, available to both roles once
 * logged in (used as the shared visual reference before Increment 1's
 * constraint submission builds on top of it).
 */
@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping
    public List<TimetableSessionDto> getAllSessions() {
        return timetableService.getAllSessions();
    }
}
