package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.service.ConstraintRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Increment 1 (FR1, FR2, FR9) - timetabling team side: view every constraint
 * request submitted by lecturers so far.
 */
@RestController
@RequestMapping("/api/timetabling-team/requests/constraints")
@RequiredArgsConstructor
public class TimetablingTeamConstraintRequestController {

    private final ConstraintRequestService constraintRequestService;

    @GetMapping
    public List<RequestDto> allRequests() {
        return constraintRequestService.getAllConstraintRequests();
    }
}
