package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ModuleDto;
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

// scoped module list (only what the logged-in lecturer actually teaches)
// constraint form's Primary Module dropdown
@RestController
@RequestMapping("/api/lecturer/my-modules")
@RequiredArgsConstructor
public class LecturerModuleController {

    private final UserRepository userRepository;
    private final TimetableService timetableService;

    @GetMapping
    public List<ModuleDto> getMyModules(Authentication authentication) {
        User lecturer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return timetableService.getModulesTaughtBy(lecturer.getId());
    }
}
