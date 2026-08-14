package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ModuleConstraintCreateDto;
import com.ntu.timetabling.dto.PersonalConstraintCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.dto.UpdateRequestStatusDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Increment 1: constraint submission (FR1, FR2, FR9).
 */
@Service
@RequiredArgsConstructor
public class ConstraintRequestService {

    private final RequestRepository requestRepository;
    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RequestDto submitModuleConstraint(String username, ModuleConstraintCreateDto dto) {
        User requester = findUser(username);

        Course department = courseRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + dto.getDepartmentId()));

        ModuleEntity primaryModule = moduleRepository.findById(dto.getPrimaryModuleId())
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + dto.getPrimaryModuleId()));

        ModuleEntity linkedModule = null;
        if (dto.getLinkedModuleId() != null) {
            linkedModule = moduleRepository.findById(dto.getLinkedModuleId())
                    .orElseThrow(() -> new EntityNotFoundException("Module not found: " + dto.getLinkedModuleId()));
        }

        Room specificRoom = null;
        if (dto.getSpecificRoomId() != null) {
            specificRoom = roomRepository.findById(dto.getSpecificRoomId())
                    .orElseThrow(() -> new EntityNotFoundException("Room not found: " + dto.getSpecificRoomId()));
        }

        WeekMode weekMode = WeekMode.valueOf(dto.getWeekMode());
        Weekday dayOfWeek = Weekday.valueOf(dto.getDayOfWeek());
        RoomType roomType = RoomType.valueOf(dto.getRoomType());
        RoomLayout preferredRoomLayout = dto.getPreferredRoomLayout() != null
                ? RoomLayout.valueOf(dto.getPreferredRoomLayout()) : RoomLayout.NONE;
        RoomFeature feature = dto.getFeature() != null ? RoomFeature.valueOf(dto.getFeature()) : RoomFeature.NONE;

        // ALL_REMAINING applies to every remaining week in the block, so no explicit week rows are needed;
        // SINGLE/MULTIPLE both just store whichever weeks are to be selected
        // SINGLE - one row from dropdown; MULTIPLE - checkboxes
        Set<Integer> weeks = weekMode == WeekMode.ALL_REMAINING
                ? new HashSet<>()
                : new HashSet<>(dto.getWeeks() == null ? Set.of() : dto.getWeeks());

        if (weekMode != WeekMode.ALL_REMAINING && weeks.isEmpty()) {
            throw new IllegalArgumentException("At least one week must be selected for weekMode " + weekMode);
        }

        Request request = Request.builder()
                .type(RequestType.CONSTRAINT)
                .constraintKind(ConstraintKind.MODULE)
                .requester(requester)
                .department(department)
                .primaryModule(primaryModule)
                .linkedModule(linkedModule)
                .additionalLinkedModules(dto.getAdditionalLinkedModules())
                .block(dto.getBlock())
                .weekMode(weekMode)
                .weeks(weeks)
                .dayOfWeek(dayOfWeek)
                .startTime(dto.getStartTime())
                .durationHours(dto.getDurationHours())
                .learningActivity(dto.getLearningActivity())
                .personalTutorDetail(dto.getPersonalTutorDetail())
                .activityDetail(dto.getActivityDetail())
                .titleTechnical(dto.getTitleTechnical())
                .roomType(roomType)
                .preferredRoomLayout(preferredRoomLayout)
                .specificRoom(specificRoom)
                .feature(feature)
                .software(dto.getSoftware())
                .supportTeamStaff(dto.getSupportTeamStaff())
                .lectureCapture(dto.getLectureCapture())
                .note(dto.getNote())
                .build();

        return RequestDto.fromEntity(requestRepository.save(request));
    }

    public RequestDto submitPersonalConstraint(String username, PersonalConstraintCreateDto dto) {
        User requester = findUser(username);

        Course department = courseRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + dto.getDepartmentId()));

        Set<Weekday> unavailableDays = dto.getUnavailableDays() == null
                ? new HashSet<>()
                : dto.getUnavailableDays().stream().map(Weekday::valueOf).collect(Collectors.toCollection(HashSet::new));

        Request request = Request.builder()
                .type(RequestType.CONSTRAINT)
                .constraintKind(ConstraintKind.PERSONAL)
                .requester(requester)
                .department(department)
                .description(dto.getDescription())
                .reason(dto.getReason())
                .unavailableDays(unavailableDays)
                .unavailableFromDate(dto.getUnavailableFromDate())
                .unavailableToDate(dto.getUnavailableToDate())
                .unavailableFromTime(dto.getUnavailableFromTime())
                .unavailableToTime(dto.getUnavailableToTime())
                .build();

        return RequestDto.fromEntity(requestRepository.save(request));
    }

    public List<RequestDto> getMyConstraintRequests(String username) {
        User requester = findUser(username);
        return requestRepository.findByRequesterIdAndType(requester.getId(), RequestType.CONSTRAINT).stream()
                .map(RequestDto::fromEntity)
                .toList();
    }

    // Timetabling Team side: can view every constraint request submitted so far.
    public List<RequestDto> getAllConstraintRequests() {
        return requestRepository.findByType(RequestType.CONSTRAINT).stream()
                .map(RequestDto::fromEntity)
                .toList();
    }

    public RequestDto getRequestById(Long id) {
        return RequestDto.fromEntity(findRequest(id));
    }

    // Timetabling Team side: move a request forward in the status flow.
    // A reason is mandatory once the decision is final (ACCEPTED or REJECTED)
    // the lecturer can view this via reasonComment but optional for the interim statuses.
    public RequestDto updateStatus(Long requestId, UpdateRequestStatusDto dto) {
        Request request = findRequest(requestId);
        RequestStatus newStatus = RequestStatus.valueOf(dto.getStatus());

        boolean isFinal = newStatus == RequestStatus.ACCEPTED || newStatus == RequestStatus.REJECTED;
        boolean hasReason = dto.getReasonComment() != null && !dto.getReasonComment().isBlank();
        if (isFinal && !hasReason) {
            throw new IllegalArgumentException("A reason is required when accepting or rejecting a request");
        }

        request.setStatus(newStatus);
        if (hasReason) {
            request.setReasonComment(dto.getReasonComment());
        }

        return RequestDto.fromEntity(requestRepository.save(request));
    }

    private Request findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
