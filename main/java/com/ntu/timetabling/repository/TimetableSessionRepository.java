package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.TimetableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableSessionRepository extends JpaRepository<TimetableSession, Long> {
    List<TimetableSession> findByLecturerId(Long lecturerId);
}
