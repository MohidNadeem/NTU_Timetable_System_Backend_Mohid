package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ChangeRequestCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Increment 2: change requests.
@Service
@RequiredArgsConstructor
public class ChangeRequestService {

    private final RequestRepository requestRepository;
    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final TimetableSessionRepository timetableSessionRepository;
    private final UserRepository userRepository;
    private final AcademicYearService academicYearService;

    public RequestDto submitChangeRequest(String username, ChangeRequestCreateDto dto) {
        User requester = findUser(username);

        Course department = courseRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + dto.getDepartmentId()));

        ModuleEntity primaryModule = moduleRepository.findById(dto.getPrimaryModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + dto.getPrimaryModuleId()));

        ChangeCategory changeCategory = ChangeCategory.valueOf(dto.getChangeCategory());
        AcademicPeriod academicPeriod = AcademicPeriod.valueOf(dto.getAcademicPeriod());
        WeekMode weekMode = WeekMode.valueOf(dto.getWeekMode());
        Weekday dayOfWeek = dto.getDayOfWeek() != null ? Weekday.valueOf(dto.getDayOfWeek()) : null;

        Set<Integer> weeks = weekMode == WeekMode.ALL_REMAINING
                ? new HashSet<>()
                : new HashSet<>(dto.getWeeks() == null ? Set.of() : dto.getWeeks());
        if (weekMode != WeekMode.ALL_REMAINING && weeks.isEmpty()) {
            throw new IllegalArgumentException("At least one week must be selected for weekMode " + weekMode);
        }

        boolean sessionOptional = changeCategory == ChangeCategory.ADDITIONAL_SESSION
                || changeCategory == ChangeCategory.MERGE_SESSIONS_GROUPS
                || changeCategory == ChangeCategory.STUDENT_ALLOCATION
                || changeCategory == ChangeCategory.OTHER;
        TimetableSession session = null;
        if (!sessionOptional) {
            if (dto.getSessionId() == null) {
                throw new IllegalArgumentException("sessionId is required for category " + changeCategory);
            }
            session = timetableSessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new EntityNotFoundException("Session not found: " + dto.getSessionId()));
        } else if (dto.getSessionId() != null) {
            session = timetableSessionRepository.findById(dto.getSessionId())
                    .orElseThrow(() -> new EntityNotFoundException("Session not found: " + dto.getSessionId()));
        }

        TimetableSession clashingSession = null;
        if (changeCategory == ChangeCategory.CLASHES) {
            if (dto.getClashingSessionId() == null) {
                throw new IllegalArgumentException("clashingSessionId is required for the Clashes category");
            }
            clashingSession = timetableSessionRepository.findById(dto.getClashingSessionId())
                    .orElseThrow(() -> new EntityNotFoundException("Session not found: " + dto.getClashingSessionId()));
        }

        User preferredNewLecturer = null;
        if (changeCategory == ChangeCategory.STAFF_CHANGE) {
            if (dto.getPreferredNewLecturerId() == null) {
                throw new IllegalArgumentException("preferredNewLecturerId is required for the Staff Change category");
            }
            preferredNewLecturer = userRepository.findById(dto.getPreferredNewLecturerId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getPreferredNewLecturerId()));
        }

        Set<TimetableSession> mergeSessions = new HashSet<>();
        if (changeCategory == ChangeCategory.MERGE_SESSIONS_GROUPS) {
            if (dto.getMergeSessionIds() == null || dto.getMergeSessionIds().size() < 2) {
                throw new IllegalArgumentException("At least 2 sessions must be selected to merge");
            }
            for (Long id : dto.getMergeSessionIds()) {
                mergeSessions.add(timetableSessionRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Session not found: " + id)));
            }
            // a merge is inherently an event for a specific week
            if (weekMode != WeekMode.SINGLE || weeks.size() != 1) {
                throw new IllegalArgumentException("Merge requests must specify exactly one week");
            }
        }

        if (changeCategory == ChangeCategory.ADDITIONAL_SESSION && dto.getDeliveryType() == null) {
            throw new IllegalArgumentException("deliveryType is required for the Additional Session category");
        }

        RoomType roomType = dto.getRoomType() != null ? RoomType.valueOf(dto.getRoomType()) : null;
        Set<Room> allowedRooms = new HashSet<>();
        if (dto.getAllowedRoomIds() != null) {
            for (Long roomId : dto.getAllowedRoomIds()) {
                allowedRooms.add(roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId)));
            }
        }

        // snapshotting the current academic year label so this request keeps reading correctly
        // even after the Timetabling Team rolls the year over later
        String yearLabel = academicYearService.getCurrent().getCurrentYearLabel();

        Request request = Request.builder()
                .type(RequestType.CHANGE)
                .requester(requester)
                .department(department)
                .primaryModule(primaryModule)
                .session(session)
                .clashingSession(clashingSession)
                .preferredNewLecturer(preferredNewLecturer)
                .mergeSessions(mergeSessions)
                .block(dto.getBlock())
                .weekMode(weekMode)
                .weeks(weeks)
                .dayOfWeek(dayOfWeek)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .learningActivity(dto.getDeliveryType())
                .roomType(roomType)
                .allowedRooms(allowedRooms)
                .changeCategory(changeCategory)
                .rationale(dto.getRationale())
                .benefitToStudents(dto.getBenefitToStudents())
                .academicPeriod(academicPeriod)
                .academicYearLabel(yearLabel)
                .build();

        return RequestDto.fromEntity(requestRepository.save(request));
    }

    public List<RequestDto> getMyChangeRequests(String username, RequestStatus status, ChangeCategory category) {
        User requester = findUser(username);
        return requestRepository.findFiltered(RequestType.CHANGE, requester.getId(), null, status, null, category)
                .stream().map(RequestDto::fromEntity).toList();
    }

    /** Timetabling Team side: see every change request submitted so far, with optional filters. */
    public List<RequestDto> getAllChangeRequests(RequestStatus status, Long departmentId, ChangeCategory category) {
        return requestRepository.findFiltered(RequestType.CHANGE, null, null, status, departmentId, category)
                .stream().map(RequestDto::fromEntity).toList();
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
