package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ModuleConstraintCreateDto;
import com.ntu.timetabling.dto.PersonalConstraintCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.dto.RequestGroupCreateDto;
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
    private final NotificationService notificationService;

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

        Set<Room> allowedRooms = new HashSet<>();
        if (dto.getAllowedRoomIds() != null) {
            for (Long roomId : dto.getAllowedRoomIds()) {
                allowedRooms.add(roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found: " + roomId)));
            }
        }

        WeekMode weekMode = WeekMode.valueOf(dto.getWeekMode());
        // day/time are now optional per evaluation feedback - null means "no preference, Academics will decide"
        Weekday dayOfWeek = dto.getDayOfWeek() != null ? Weekday.valueOf(dto.getDayOfWeek()) : null;
        RoomType roomType = RoomType.valueOf(dto.getRoomType());
        RoomLayout preferredRoomLayout = dto.getPreferredRoomLayout() != null
                ? RoomLayout.valueOf(dto.getPreferredRoomLayout()) : RoomLayout.NONE;
        RoomFeature feature = dto.getFeature() != null ? RoomFeature.valueOf(dto.getFeature()) : RoomFeature.NONE;

        // ALL_REMAINING applies to every remaining week in the block, so no explicit week rows are needed;
        // SINGLE/MULTIPLE both just store whichever weeks were selected (SINGLE happens to be one row)
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
                .allowedRooms(allowedRooms)
                .feature(feature)
                .software(dto.getSoftware())
                .supportTeamStaff(dto.getSupportTeamStaff())
                .lectureCapture(dto.getLectureCapture())
                .note(dto.getNote())
                .build();

        // building groups after the request itself so each RequestGroup can reference its parent
        if (dto.getGroups() != null) {
            for (RequestGroupCreateDto groupDto : dto.getGroups()) {
                User preferredLecturer = null;
                if (groupDto.getPreferredLecturerId() != null) {
                    preferredLecturer = userRepository.findById(groupDto.getPreferredLecturerId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found: " + groupDto.getPreferredLecturerId()));
                }
                request.getGroups().add(RequestGroup.builder()
                        .request(request)
                        .groupLabel(groupDto.getGroupLabel())
                        .preferredLecturer(preferredLecturer)
                        .build());
            }
        }

        Request saved = requestRepository.save(request);
        notificationService.notifyAllTimetablingTeam("NEW_REQUEST",
                requester.getFullName() + " submitted a module-based constraint for " + primaryModule.getCode() + ".",
                saved);
        return RequestDto.fromEntity(saved);
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

        Request saved = requestRepository.save(request);
        notificationService.notifyAllTimetablingTeam("NEW_REQUEST",
                requester.getFullName() + " submitted a personal constraint.", saved);
        return RequestDto.fromEntity(saved);
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

        Request saved = requestRepository.save(request);
        String what = request.getConstraintKind() != null ? "constraint" : "change request";
        notificationService.notify(request.getRequester(), "REQUEST_STATUS_CHANGED",
                "Your " + what + " (#" + request.getId() + ") is now " + newStatus.name().toLowerCase().replace('_', ' ') + ".",
                request, null);

        return RequestDto.fromEntity(saved);
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
