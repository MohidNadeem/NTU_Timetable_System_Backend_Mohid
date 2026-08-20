package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CancelSessionDto;
import com.ntu.timetabling.dto.SessionCreateDto;
import com.ntu.timetabling.dto.SessionUpdateResultDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.dto.UpdateSessionDto;
import com.ntu.timetabling.service.SessionUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetabling-team/sessions")
@RequiredArgsConstructor
public class SessionUpdateController {

    private final SessionUpdateService sessionUpdateService;

    @GetMapping("/{id}")
    public TimetableSessionDto getSession(@PathVariable Long id) {
        return sessionUpdateService.getSession(id);
    }

    @PutMapping("/{id}")
    public SessionUpdateResultDto updateSession(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateSessionDto dto,
                                                 Authentication authentication) {
        return sessionUpdateService.updateSession(id, dto, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<TimetableSessionDto> createSession(@Valid @RequestBody SessionCreateDto dto,
                                                               Authentication authentication) {
        TimetableSessionDto created = sessionUpdateService.createSession(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/cancel")
    public TimetableSessionDto cancelSession(@PathVariable Long id,
                                              @Valid @RequestBody CancelSessionDto dto,
                                              Authentication authentication) {
        return sessionUpdateService.cancelSession(id, dto, authentication.getName());
    }
}
