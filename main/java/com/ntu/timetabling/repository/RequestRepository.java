package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterIdAndType(Long requesterId, RequestType type);
    List<Request> findByType(RequestType type);
}
