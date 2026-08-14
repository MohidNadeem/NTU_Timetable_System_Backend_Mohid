package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.SessionUpdateResultDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.dto.UpdateSessionDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * scope = ALL_REMAINING updates the base TimetableSession's recurring pattern
 * scope = SINGLE or MULTIPLE instead creates a SessionOverride for just those week(s),
 * leaving the base pattern untouched for every other week.
 */
@Service
@RequiredArgsConstructor
public class SessionUpdateService {

    private final TimetableSessionRepository timetableSessionRepository;
    private final SessionOverrideRepository sessionOverrideRepository;
    private final RoomRepository roomRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public TimetableSessionDto getSession(Long sessionId) {
        TimetableSession session = findSession(sessionId);
        return TimetableSessionDto.fromEntity(session);
    }

    public SessionUpdateResultDto updateSession(Long sessionId, UpdateSessionDto dto, String actingUsername) {
        TimetableSession session = findSession(sessionId);
        User actingUser = userRepository.findByUsername(actingUsername)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        Weekday newDay = Weekday.valueOf(dto.getDayOfWeek());
        WeekMode scope = WeekMode.valueOf(dto.getScope());

        Room newRoom = null;
        if (dto.getRoomId() != null) {
            newRoom = roomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found: " + dto.getRoomId()));
        }

        Request relatedRequest = null;
        if (dto.getRelatedRequestId() != null) {
            relatedRequest = requestRepository.findById(dto.getRelatedRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + dto.getRelatedRequestId()));
        }

        if (scope == WeekMode.ALL_REMAINING) {
            session.setDayOfWeek(newDay);
            session.setStartTime(dto.getStartTime());
            session.setEndTime(dto.getEndTime());
            if (newRoom != null) {
                session.setRoom(newRoom);
            }
            timetableSessionRepository.save(session);

            return SessionUpdateResultDto.builder()
                    .sessionId(session.getId())
                    .scope(scope.name())
                    .appliedToBasePattern(true)
                    .build();
        }

        // SINGLE or MULTIPLE - create an override rather than touching the base pattern
        Set<Integer> weeks = dto.getWeeks() == null ? Set.of() : new HashSet<>(dto.getWeeks());
        if (weeks.isEmpty()) {
            throw new IllegalArgumentException("At least one week must be selected for scope " + scope);
        }

        SessionOverride override = SessionOverride.builder()
                .session(session)
                .newDayOfWeek(newDay)
                .newStartTime(dto.getStartTime())
                .newEndTime(dto.getEndTime())
                .newRoom(newRoom)
                .weeks(weeks)
                .relatedRequest(relatedRequest)
                .reason(dto.getReason())
                .createdBy(actingUser)
                .build();

        SessionOverride saved = sessionOverrideRepository.save(override);

        return SessionUpdateResultDto.builder()
                .sessionId(session.getId())
                .scope(scope.name())
                .appliedToBasePattern(false)
                .overrideId(saved.getId())
                .build();
    }

    private TimetableSession findSession(Long id) {
        return timetableSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + id));
    }
}
