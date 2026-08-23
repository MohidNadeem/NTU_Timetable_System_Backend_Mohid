package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.CancelSessionDto;
import com.ntu.timetabling.dto.SessionCreateDto;
import com.ntu.timetabling.dto.SessionUpdateResultDto;
import com.ntu.timetabling.dto.TimetableSessionDto;
import com.ntu.timetabling.dto.UpdateSessionDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Timetabling Team's "Update Session" feature - reusable for both resolving
 * a violation and for Increment 2's change requests later.
 *
 * scope = ALL_REMAINING updates the base TimetableSession's recurring
 * pattern directly (a genuine change going forward). scope = SINGLE or
 * MULTIPLE instead creates a SessionOverride for just those week(s),
 * leaving the base pattern untouched for every other week.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SessionUpdateService {

    private final TimetableSessionRepository timetableSessionRepository;
    private final SessionOverrideRepository sessionOverrideRepository;
    private final RoomRepository roomRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final ClashCheckService clashCheckService;
    private final CourseChangeNotificationService courseChangeNotificationService;

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

        User newLecturer = null;
        if (dto.getLecturerId() != null) {
            newLecturer = userRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getLecturerId()));
            if (scope != WeekMode.ALL_REMAINING) {
                throw new IllegalArgumentException(
                        "Reassigning the teacher is only supported for All Weeks Ahead scope - session_overrides doesn't carry a per-week teacher");
            }
        }

        Request relatedRequest = null;
        if (dto.getRelatedRequestId() != null) {
            relatedRequest = requestRepository.findById(dto.getRelatedRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + dto.getRelatedRequestId()));
        }

        if (scope == WeekMode.ALL_REMAINING) {
            Room roomForCheck = newRoom != null ? newRoom : session.getRoom();
            User lecturerForCheck = newLecturer != null ? newLecturer : session.getLecturer();
            clashCheckService.assertNoClash(session.getId(), session.getBlock(), newDay,
                    dto.getStartTime(), dto.getEndTime(), roomForCheck, lecturerForCheck);

            session.setDayOfWeek(newDay);
            session.setStartTime(dto.getStartTime());
            session.setEndTime(dto.getEndTime());
            if (newRoom != null) {
                session.setRoom(newRoom);
            }
            if (newLecturer != null) {
                session.setLecturer(newLecturer);
            }
            timetableSessionRepository.save(session);
            courseChangeNotificationService.notifySessionUpdated(session);

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

        Room roomForCheck = newRoom != null ? newRoom : session.getRoom();
        clashCheckService.assertNoClash(session.getId(), session.getBlock(), newDay,
                dto.getStartTime(), dto.getEndTime(), roomForCheck, session.getLecturer());

        // reconciling any existing overrides that already claim some of these weeks
        reconcileOverlappingOverrides(session, weeks);

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
        courseChangeNotificationService.notifySessionUpdatedForWeeks(
                session, weeks, newDay.name(), dto.getStartTime(), dto.getEndTime(), newRoom);

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

    // "Add Session" - creates a new session to fulfil an Additional Session change request
    public TimetableSessionDto createSession(SessionCreateDto dto, String actingUsername) {
        ModuleEntity module = moduleRepository.findById(dto.getModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + dto.getModuleId()));
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found: " + dto.getRoomId()));

        Request relatedRequest = null;
        if (dto.getRelatedRequestId() != null) {
            relatedRequest = requestRepository.findById(dto.getRelatedRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + dto.getRelatedRequestId()));
        }

        User lecturer;
        if (dto.getLecturerId() != null) {
            lecturer = userRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getLecturerId()));
        } else if (relatedRequest != null) {
            lecturer = relatedRequest.getRequester();
        } else {
            throw new IllegalArgumentException("A teacher must be selected when creating a session with no related request");
        }

        TimetableSession session = TimetableSession.builder()
                .module(module)
                .room(room)
                .lecturer(lecturer)
                .sessionType(SessionType.valueOf(dto.getSessionType()))
                .dayOfWeek(Weekday.valueOf(dto.getDayOfWeek()))
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .block(dto.getBlock())
                .relatedRequest(relatedRequest)
                .sessionLabel(dto.getSessionLabel())
                // auto-tagging with every course that offers this module
                .courses(new java.util.HashSet<>(module.getCourses()))
                .build();

        clashCheckService.assertNoClash(null, dto.getBlock(), session.getDayOfWeek(),
                session.getStartTime(), session.getEndTime(), room, lecturer);

        TimetableSession saved = timetableSessionRepository.save(session);

        if (dto.getRestrictToWeeks() != null && !dto.getRestrictToWeeks().isEmpty()) {
            // making this a one-week (or few-week) session rather than a normal recurring one
            int totalWeeks = AcademicBlockConfig.weeksInBlock(dto.getBlock());
            for (int week = 1; week <= totalWeeks; week++) {
                if (!dto.getRestrictToWeeks().contains(week)) {
                    saved.getCancelledWeeks().add(week);
                }
            }
            saved = timetableSessionRepository.save(saved);
        }

        courseChangeNotificationService.notifySessionCreated(saved);
        return TimetableSessionDto.fromEntity(saved);
    }

    // "Cancel Session" (Session Removal category, and the removal step of a Merge)
    public TimetableSessionDto cancelSession(Long sessionId, CancelSessionDto dto, String actingUsername) {
        TimetableSession session = findSession(sessionId);

        Request relatedRequest = null;
        if (dto.getRelatedRequestId() != null) {
            relatedRequest = requestRepository.findById(dto.getRelatedRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Request not found: " + dto.getRelatedRequestId()));
        }

        WeekMode scope = WeekMode.valueOf(dto.getScope());
        Set<Integer> weeksBeingCancelled = null;

        if (scope == WeekMode.ALL_REMAINING) {
            session.setCancelledAt(java.time.LocalDateTime.now());
            session.setCancelledByRequest(relatedRequest);
        } else {
            weeksBeingCancelled = dto.getWeeks() == null ? Set.of() : new HashSet<>(dto.getWeeks());
            if (weeksBeingCancelled.isEmpty()) {
                throw new IllegalArgumentException("At least one week must be selected for scope " + scope);
            }
            session.getCancelledWeeks().addAll(weeksBeingCancelled);
        }

        TimetableSession saved = timetableSessionRepository.save(session);
        if (scope == WeekMode.ALL_REMAINING) {
            courseChangeNotificationService.notifySessionCancelled(saved);
        } else {
            courseChangeNotificationService.notifySessionCancelledForWeeks(saved, weeksBeingCancelled);
        }
        return TimetableSessionDto.fromEntity(saved);
    }

    // ensures no week is ever claimed by more than one override for the same session
    private void reconcileOverlappingOverrides(TimetableSession session, Set<Integer> newWeeks) {
        for (SessionOverride existing : sessionOverrideRepository.findBySessionId(session.getId())) {
            if (existing.getWeeks().stream().noneMatch(newWeeks::contains)) {
                continue; // no overlap - leave this override untouched
            }
            Set<Integer> remaining = new HashSet<>(existing.getWeeks());
            remaining.removeAll(newWeeks);
            if (remaining.isEmpty()) {
                sessionOverrideRepository.delete(existing);
            } else {
                existing.setWeeks(remaining);
                sessionOverrideRepository.save(existing);
            }
        }
    }
}
