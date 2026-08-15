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

        TimetableSession currentSession = timetableSessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + dto.getSessionId()));

        PreferredRoomAnswer preferredRoomAnswer = PreferredRoomAnswer.valueOf(dto.getPreferredRoomAnswer());
        Room specificRoom = null;
        if (preferredRoomAnswer == PreferredRoomAnswer.YES && dto.getSpecificRoomId() != null) {
            specificRoom = roomRepository.findById(dto.getSpecificRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found: " + dto.getSpecificRoomId()));
        } else if (preferredRoomAnswer == PreferredRoomAnswer.ONLINE) {
            // resolving the seeded pseudo-room automatically so effect-checking always has a
            // concrete room to compare against, without the frontend needing to know its id
            specificRoom = roomRepository.findByName("ONLINE").orElse(null);
        }

        WeekMode weekMode = WeekMode.valueOf(dto.getWeekMode());
        Weekday dayOfWeek = dto.getDayOfWeek() != null ? Weekday.valueOf(dto.getDayOfWeek()) : null;
        AcademicPeriod academicPeriod = AcademicPeriod.valueOf(dto.getAcademicPeriod());
        ChangeCategory changeCategory = ChangeCategory.valueOf(dto.getChangeCategory());

        Set<Integer> weeks = weekMode == WeekMode.ALL_REMAINING
                ? new HashSet<>()
                : new HashSet<>(dto.getWeeks() == null ? Set.of() : dto.getWeeks());

        if (weekMode != WeekMode.ALL_REMAINING && weeks.isEmpty()) {
            throw new IllegalArgumentException("At least one week must be selected for weekMode " + weekMode);
        }

        // snapshotting the current academic year label so this request keeps reading correctly
        // even after the Timetabling Team rolls the year over later
        String yearLabel = academicYearService.getCurrent().getCurrentYearLabel();

        Request request = Request.builder()
                .type(RequestType.CHANGE)
                .requester(requester)
                .department(department)
                .primaryModule(primaryModule)
                .session(currentSession)
                .block(dto.getBlock())
                .weekMode(weekMode)
                .weeks(weeks)
                .dayOfWeek(dayOfWeek)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .learningActivity(dto.getDeliveryType())
                .roomBookingNeeded(dto.getRoomBookingNeeded())
                .preferredRoomAnswer(preferredRoomAnswer)
                .specificRoom(specificRoom)
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
