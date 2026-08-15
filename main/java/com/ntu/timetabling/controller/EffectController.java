package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.EffectResultDto;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.service.EffectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * "View Effect" - runs the same calculator used by Violations/Changes in
 * Queue against a single request, regardless of its current status.
 *
 * Lets the Timetabling Team preview what accepting a request would mean for the
 * schedule before actually deciding.
 */
@RestController
@RequestMapping("/api/timetabling-team/requests")
@RequiredArgsConstructor
public class EffectController {

    private final RequestRepository requestRepository;
    private final EffectService effectService;

    @GetMapping("/{id}/effect")
    public EffectResultDto getEffect(@PathVariable Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));
        return effectService.computeEffect(request);
    }
}
