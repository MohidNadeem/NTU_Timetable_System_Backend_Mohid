package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.SessionUpdateResultDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.dto.UpdateSessionDto;
import com.ntu.timetabling.service.SessionUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
