package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.SessionOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionOverrideRepository extends JpaRepository<SessionOverride, Long> {
    List<SessionOverride> findBySessionId(Long sessionId);
}
