package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.CourseCreateUpdateDto;
import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.repository.CourseRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Course create(CourseCreateUpdateDto dto) {
        if (courseRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Course code already in use: " + dto.getCode());
        }
        return courseRepository.save(Course.builder().code(dto.getCode()).name(dto.getName()).build());
    }

    public Course update(Long id, CourseCreateUpdateDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
        course.setCode(dto.getCode());
        course.setName(dto.getName());
        return courseRepository.save(course);
    }

    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
        // guarding against orphaning students still enrolled in this course
        if (userRepository.existsByCourseId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete " + course.getCode() + " - students are still enrolled in it. Reassign them first.");
        }
        courseRepository.delete(course);
    }
}
