package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CourseDto;
import com.ntu.timetabling.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// read-only lookup, available to any authenticated user
// populates the timetable's course filter dropdown
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;

    @GetMapping
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(CourseDto::fromEntity)
                .toList();
    }
}
