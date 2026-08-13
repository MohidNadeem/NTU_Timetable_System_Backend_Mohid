package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
