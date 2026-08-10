package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ConstraintRequestCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.service.ConstraintRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Increment 1 (FR1, FR2, FR9) - lecturer side: submit a constraint request
 * ahead of the annual timetable, and view their own past submissions.
 */
@RestController
@RequestMapping("/api/lecturer/requests/constraints")
@RequiredArgsConstructor
public class LecturerConstraintRequestController {

    private final ConstraintRequestService constraintRequestService;

    @PostMapping
    public ResponseEntity<RequestDto> submit(Authentication authentication,
                                              @Valid @RequestBody ConstraintRequestCreateDto dto) {
        RequestDto created = constraintRequestService.submitConstraintRequest(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<RequestDto> myRequests(Authentication authentication) {
        return constraintRequestService.getMyConstraintRequests(authentication.getName());
    }
}
