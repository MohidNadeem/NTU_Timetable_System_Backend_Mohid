package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.UserRepository;
import com.ntu.timetabling.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// scoped session list
// for the change request form's "current session" picker (Q8-10 on the MS form: pick from a real scheduled
// session instead of typing a date manually)
@RestController
@RequestMapping("/api/lecturer/my-sessions")
@RequiredArgsConstructor
public class LecturerSessionController {

    private final UserRepository userRepository;
    private final TimetableService timetableService;

    @GetMapping
    public List<TimetableSessionDto> getMySessions(Authentication authentication) {
        User lecturer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return timetableService.getSessionsForLecturer(lecturer.getId());
    }
}
