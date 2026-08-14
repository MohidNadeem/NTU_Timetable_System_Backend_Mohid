package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ViolationDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Not doing full timetable generation here - instead,
 * this scans every ACCEPTED module-based constraint and checks whether
 * the timetable still actually matches what was agreed.
 * Anything that doesn't match shows up on the Violations page for the Timetabling Team to fix.
 */
@Service
@RequiredArgsConstructor
public class ViolationService {

    private final RequestRepository requestRepository;
    private final TimetableSessionRepository timetableSessionRepository;

    public List<ViolationDto> getViolations() {
        return requestRepository.findByType(RequestType.CONSTRAINT).stream()
                .filter(r -> r.getConstraintKind() == ConstraintKind.MODULE)
                .filter(r -> r.getStatus() == RequestStatus.ACCEPTED)
                .map(this::checkRequest)
                .filter(v -> v != null) // null = fully matches, not a violation
                .toList();
    }

    private ViolationDto checkRequest(Request r) {
        ModuleEntity module = r.getPrimaryModule();
        if (module == null) return null; // shouldn't happen for Module-based requests

        SessionType inferredType = inferSessionType(r.getLearningActivity());

        TimetableSession matched = timetableSessionRepository.findByModuleId(module.getId()).stream()
                .filter(s -> inferredType == null || s.getSessionType() == inferredType)
                .findFirst()
                .orElse(null);

        String requestedRoomName = r.getSpecificRoom() != null ? r.getSpecificRoom().getName() : null;

        if (matched == null) {
            return ViolationDto.builder()
                    .requestId(r.getId())
                    .requesterName(r.getRequester().getFullName())
                    .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null)
                    .primaryModuleCode(module.getCode())
                    .primaryModuleName(module.getName())
                    .block(r.getBlock())
                    .learningActivity(r.getLearningActivity())
                    .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                    .requestedStartTime(r.getStartTime())
                    .requestedDurationHours(r.getDurationHours())
                    .requestedRoomName(requestedRoomName)
                    .hasMatchedSession(false)
                    .build();
        }

        boolean dayMismatch = r.getDayOfWeek() != null && !r.getDayOfWeek().equals(matched.getDayOfWeek());
        boolean timeMismatch = r.getStartTime() != null && !r.getStartTime().equals(matched.getStartTime());
        boolean roomMismatch = requestedRoomName != null && !requestedRoomName.equals(matched.getRoom().getName());

        if (!dayMismatch && !timeMismatch && !roomMismatch) {
            return null; // matches what's currently scheduled - no violation
        }

        return ViolationDto.builder()
                .requestId(r.getId())
                .requesterName(r.getRequester().getFullName())
                .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null)
                .primaryModuleCode(module.getCode())
                .primaryModuleName(module.getName())
                .block(r.getBlock())
                .learningActivity(r.getLearningActivity())
                .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                .requestedStartTime(r.getStartTime())
                .requestedDurationHours(r.getDurationHours())
                .requestedRoomName(requestedRoomName)
                .hasMatchedSession(true)
                .matchedSessionId(matched.getId())
                .matchedSessionType(matched.getSessionType().name())
                .currentDayOfWeek(matched.getDayOfWeek().name())
                .currentStartTime(matched.getStartTime())
                .currentEndTime(matched.getEndTime())
                .currentRoomName(matched.getRoom().getName())
                .build();
    }

    private SessionType inferSessionType(String learningActivity) {
        if (learningActivity == null) return null;
        String a = learningActivity.toLowerCase(Locale.ROOT);
        if (a.contains("lab") || a.contains("practical")) return SessionType.LAB;
        if (a.contains("seminar")) return SessionType.SEMINAR;
        if (a.contains("tutorial")) return SessionType.TUTORIAL;
        if (a.contains("surgery")) return SessionType.SURGERY;
        if (a.contains("lecture")) return SessionType.LECTURE;
        return null;
    }
}
