package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.dto.UpdateRequestStatusDto;
import com.ntu.timetabling.service.ConstraintRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * detail/status endpoints
 * This is the endpoint the frontend's request detail page uses going forward,
 * regardless of which list (constraints or changes) it was opened from.
 */
@RestController
@RequestMapping("/api/timetabling-team/requests")
@RequiredArgsConstructor
public class RequestReviewController {

    private final ConstraintRequestService constraintRequestService;

    @GetMapping("/{id}")
    public RequestDto getRequest(@PathVariable Long id) {
        return constraintRequestService.getRequestById(id);
    }

    @PutMapping("/{id}/status")
    public RequestDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRequestStatusDto dto) {
        return constraintRequestService.updateStatus(id, dto);
    }
}
