package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.EffectResultDto;
import com.ntu.timetabling.service.ChangesInQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetabling-team/changes-in-queue")
@RequiredArgsConstructor
public class ChangesInQueueController {

    private final ChangesInQueueService changesInQueueService;

    @GetMapping
    public List<EffectResultDto> getChangesInQueue() {
        return changesInQueueService.getChangesInQueue();
    }
}
