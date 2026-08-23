package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.service.ConstraintRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// shared single-request detail fetch, for the new lecturer-facing detail page (detail + chat)
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestDetailController {

    private final ConstraintRequestService constraintRequestService;

    @GetMapping("/{id}")
    public RequestDto getRequest(@PathVariable Long id, Authentication authentication) {
        return constraintRequestService.getRequestByIdForUser(id, authentication.getName());
    }
}
