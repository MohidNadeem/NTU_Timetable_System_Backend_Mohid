package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.model.ChangeCategory;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetabling-team/requests/changes")
@RequiredArgsConstructor
public class TimetablingTeamChangeRequestController {

    private final ChangeRequestService changeRequestService;

    @GetMapping
    public List<RequestDto> allRequests(@RequestParam(required = false) RequestStatus status,
                                         @RequestParam(required = false) Long departmentId,
                                         @RequestParam(required = false) ChangeCategory category) {
        return changeRequestService.getAllChangeRequests(status, departmentId, category);
    }
}
