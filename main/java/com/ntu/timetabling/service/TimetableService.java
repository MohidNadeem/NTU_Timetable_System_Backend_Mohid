package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ModuleDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.model.ModuleEntity;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Increment 0 - read-only access to the mock/seeded timetable, used to give
 * both roles something visual to look at before Increment 1's constraint
 * submission feature is wired up on top of it.
 */
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableSessionRepository timetableSessionRepository;

    public List<TimetableSessionDto> getAllSessions() {
        return timetableSessionRepository.findAll().stream()
                .map(TimetableSessionDto::fromEntity)
                .toList();
    }

    public List<TimetableSessionDto> getSessionsForLecturer(Long lecturerId) {
        return timetableSessionRepository.findByLecturerId(lecturerId).stream()
                .map(TimetableSessionDto::fromEntity)
                .toList();
    }

    // adding filters to the timetable screen's week (block) + course + teacher + room filters
    // all params optional
    public List<TimetableSessionDto> getFilteredSessions(Integer block, Long courseId, Long lecturerId, Long roomId) {
        return timetableSessionRepository.findFiltered(block, courseId, lecturerId, roomId).stream()
                .map(TimetableSessionDto::fromEntity)
                .toList();
    }

    // reused by the lecturer dashboard's teaching-load list
    // & the constraint form's Primary Module dropdown
    public List<ModuleDto> getModulesTaughtBy(Long lecturerId) {
        Map<Long, ModuleDto> byModuleId = new LinkedHashMap<>();
        for (TimetableSession session : timetableSessionRepository.findByLecturerIdOrderByBlockAsc(lecturerId)) {
            ModuleEntity module = session.getModule();
            byModuleId.putIfAbsent(module.getId(), ModuleDto.fromEntity(module, session.getBlock()));
        }
        return byModuleId.values().stream().toList();
    }
}
