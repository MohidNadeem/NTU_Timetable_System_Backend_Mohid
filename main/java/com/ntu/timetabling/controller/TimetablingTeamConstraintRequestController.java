package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.dto.UpdateRequestStatusDto;
import com.ntu.timetabling.service.ConstraintRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Timetabling Team side: view every constraint request submitted so far,
 * open one in full detail, and move it through the status flow
 * a reason is required once the decision is final (ACCEPTED or REJECTED).
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

    @GetMapping("/{id}")
    public RequestDto getRequest(@PathVariable Long id) {
        return constraintRequestService.getRequestById(id);
    }

    @PutMapping("/{id}/status")
    public RequestDto updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRequestStatusDto dto) {
        return constraintRequestService.updateStatus(id, dto);
    }
}
