package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ViolationDto;
import com.ntu.timetabling.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetabling-team/violations")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    @GetMapping
    public List<ViolationDto> getViolations() {
        return violationService.getViolations();
    }
}
