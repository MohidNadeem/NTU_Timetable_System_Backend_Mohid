package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ModuleDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.model.ModuleEntity;
import com.ntu.timetabling.model.SessionOverride;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.repository.SessionOverrideRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Increment 0 - read-only access to the mock/seeded timetable, used to give
 * both roles something visual to look at before Increment 1's constraint
 * submission feature is wired up on top of it.
 */
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final TimetableSessionRepository timetableSessionRepository;
    private final SessionOverrideRepository sessionOverrideRepository;

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
    public List<TimetableSessionDto> getFilteredSessions(Integer block, Integer week, Long courseId, Long lecturerId, Long roomId) {
        return timetableSessionRepository.findFiltered(block, courseId, lecturerId, roomId).stream()
                .filter(s -> week != null ? s.isActiveInWeek(week) : !s.isFullyCancelled())
                .map(s -> applyOverrideIfAny(s, week))
                .toList();
    }

    private TimetableSessionDto applyOverrideIfAny(TimetableSession s, Integer week) {
        if (week == null) {
            return TimetableSessionDto.fromEntity(s);
        }

        Optional<SessionOverride> match = sessionOverrideRepository.findBySessionId(s.getId()).stream()
                .filter(o -> o.getWeeks().contains(week))
                .findFirst();

        if (match.isEmpty()) {
            return TimetableSessionDto.fromEntity(s);
        }

        SessionOverride o = match.get();
        return TimetableSessionDto.builder()
                .id(s.getId())
                .moduleCode(s.getModule().getCode())
                .moduleName(s.getModule().getName())
                .roomName(o.getNewRoom() != null ? o.getNewRoom().getName() : s.getRoom().getName())
                .roomBuilding(o.getNewRoom() != null ? o.getNewRoom().getBuilding() : s.getRoom().getBuilding())
                .lecturerName(s.getLecturer().getFullName())
                .sessionType(s.getSessionType())
                .dayOfWeek(o.getNewDayOfWeek())
                .startTime(o.getNewStartTime())
                .endTime(o.getNewEndTime())
                .block(s.getBlock())
                .partNumber(s.getPartNumber())
                .sessionLabel(s.getSessionLabel())
                .courseCodes(s.getCourses().stream().map(c -> c.getCode()).sorted().toList())
                .isOverridden(true)
                .build();
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
