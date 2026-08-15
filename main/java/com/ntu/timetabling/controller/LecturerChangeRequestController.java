package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ChangeRequestCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.model.ChangeCategory;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.service.ChangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer/requests/changes")
@RequiredArgsConstructor
public class LecturerChangeRequestController {

    private final ChangeRequestService changeRequestService;

    @PostMapping
    public ResponseEntity<RequestDto> submit(Authentication authentication,
                                              @Valid @RequestBody ChangeRequestCreateDto dto) {
        RequestDto created = changeRequestService.submitChangeRequest(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<RequestDto> myRequests(Authentication authentication,
                                        @RequestParam(required = false) RequestStatus status,
                                        @RequestParam(required = false) ChangeCategory category) {
        return changeRequestService.getMyChangeRequests(authentication.getName(), status, category);
    }
}
