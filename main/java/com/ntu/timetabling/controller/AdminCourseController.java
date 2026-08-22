package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CourseCreateUpdateDto;
import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping
    public List<Course> getAll() {
        return adminCourseService.getAll();
    }

    @PostMapping
    public ResponseEntity<Course> create(@Valid @RequestBody CourseCreateUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminCourseService.create(dto));
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @Valid @RequestBody CourseCreateUpdateDto dto) {
        return adminCourseService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminCourseService.delete(id);
    }
}
