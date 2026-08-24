package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ActivityLogDto;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.UserRepository;
import com.ntu.timetabling.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// three separate role-gated endpoints
@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @GetMapping("/api/timetabling-team/activity-log")
    public List<ActivityLogDto> getForTeam() {
        return activityLogService.getForTeam();
    }

    @GetMapping("/api/lecturer/activity-log")
    public List<ActivityLogDto> getForLecturer(Authentication authentication) {
        User lecturer = findUser(authentication.getName());
        return activityLogService.getForLecturer(lecturer.getId());
    }

    @GetMapping("/api/student/activity-log")
    public List<ActivityLogDto> getForStudent(Authentication authentication) {
        User student = findUser(authentication.getName());
        return activityLogService.getForStudent(student);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
