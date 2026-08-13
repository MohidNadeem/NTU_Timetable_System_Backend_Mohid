package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.TeacherDto;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// read-only lookup, available to any authenticated user
// populates the timetable's teacher filter dropdown
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final UserRepository userRepository;

    @GetMapping
    public List<TeacherDto> getAllTeachers() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.LECTURER).stream()
                .map(TeacherDto::fromEntity)
                .toList();
    }
}
