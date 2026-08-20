package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private RequestType type;
    private ConstraintKind constraintKind;
    private Long requesterId;
    private String requesterName;
    private RequestStatus status;

    // legacy / shared
    private Boolean isFirm;
    private String category;
    private String description;
    private String reason;
    private String reasonComment;

    // module-based / change fields (shared - department/module/block/weekMode/weeks/dayOfWeek/
    // startTime apply to both MODULE constraints and CHANGE requests)
    private String departmentCode;
    private String departmentName;
    private Long primaryModuleId;
    private String primaryModuleCode;
    private String primaryModuleName;
    private String linkedModuleCode;
    private String additionalLinkedModules;
    private Integer block;
    private WeekMode weekMode;
    private Set<Integer> weeks;
    private Weekday dayOfWeek;
    private LocalTime startTime;
    private Integer durationHours;
    private String learningActivity;
    private String personalTutorDetail;
    private String activityDetail;
    private String titleTechnical;
    private String campus;
    private RoomType roomType;
    private RoomLayout preferredRoomLayout;
    private Long specificRoomId;
    private String specificRoomName;
    private List<String> allowedRoomNames;
    private List<RequestGroupDto> groups;
    private RoomFeature feature;
    private String software;
    private String supportTeamStaff;
    private Boolean lectureCapture;
    private String note;

    // personal-constraint fields (null/empty for MODULE requests)
    private Set<Weekday> unavailableDays;
    private LocalDate unavailableFromDate;
    private LocalDate unavailableToDate;
    private LocalTime unavailableFromTime;
    private LocalTime unavailableToTime;

    // CHANGE-request fields (null for CONSTRAINT requests)
    private Long currentSessionId;
    private String currentSessionSummary; // e.g. "TUE 09:00-11:00 · ERD105" - the actual scheduled session being asked about
    private Long clashingSessionId;
    private String clashingSessionSummary; // CLASHES category only
    private Long preferredNewLecturerId;
    private String preferredNewLecturerName; // STAFF_CHANGE category only
    private List<MergeSessionDto> mergeSessions; // MERGE_SESSIONS_GROUPS category only
    private LocalTime endTime;
    private Boolean roomBookingNeeded;
    private PreferredRoomAnswer preferredRoomAnswer;
    private ChangeCategory changeCategory;
    private String rationale;
    private String benefitToStudents;
    private AcademicPeriod academicPeriod;
    private String academicYearLabel;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .type(r.getType())
                .constraintKind(r.getConstraintKind())
                .requesterId(r.getRequester().getId())
                .requesterName(r.getRequester().getFullName())
                .status(r.getStatus())
                .isFirm(r.getIsFirm())
                .category(r.getCategory())
                .description(r.getDescription())
                .reason(r.getReason())
                .reasonComment(r.getReasonComment())
                .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null)
                .departmentName(r.getDepartment() != null ? r.getDepartment().getName() : null)
                .primaryModuleId(r.getPrimaryModule() != null ? r.getPrimaryModule().getId() : null)
                .primaryModuleCode(r.getPrimaryModule() != null ? r.getPrimaryModule().getCode() : null)
                .primaryModuleName(r.getPrimaryModule() != null ? r.getPrimaryModule().getName() : null)
                .linkedModuleCode(r.getLinkedModule() != null ? r.getLinkedModule().getCode() : null)
                .additionalLinkedModules(r.getAdditionalLinkedModules())
                .block(r.getBlock())
                .weekMode(r.getWeekMode())
                .weeks(r.getWeeks())
                .dayOfWeek(r.getDayOfWeek())
                .startTime(r.getStartTime())
                .durationHours(r.getDurationHours())
                .learningActivity(r.getLearningActivity())
                .personalTutorDetail(r.getPersonalTutorDetail())
                .activityDetail(r.getActivityDetail())
                .titleTechnical(r.getTitleTechnical())
                .campus(r.getCampus())
                .roomType(r.getRoomType())
                .preferredRoomLayout(r.getPreferredRoomLayout())
                .specificRoomId(r.getSpecificRoom() != null ? r.getSpecificRoom().getId() : null)
                .specificRoomName(r.getSpecificRoom() != null ? r.getSpecificRoom().getName() : null)
                .allowedRoomNames(r.getAllowedRooms().stream().map(Room::getName).sorted().toList())
                .groups(r.getGroups().stream().map(RequestGroupDto::fromEntity).toList())
                .feature(r.getFeature())
                .software(r.getSoftware())
                .supportTeamStaff(r.getSupportTeamStaff())
                .lectureCapture(r.getLectureCapture())
                .note(r.getNote())
                .unavailableDays(r.getUnavailableDays())
                .unavailableFromDate(r.getUnavailableFromDate())
                .unavailableToDate(r.getUnavailableToDate())
                .unavailableFromTime(r.getUnavailableFromTime())
                .unavailableToTime(r.getUnavailableToTime())
                .currentSessionId(r.getSession() != null ? r.getSession().getId() : null)
                .currentSessionSummary(r.getSession() != null ? summariseSession(r.getSession()) : null)
                .clashingSessionId(r.getClashingSession() != null ? r.getClashingSession().getId() : null)
                .clashingSessionSummary(r.getClashingSession() != null ? summariseSessionWithModule(r.getClashingSession()) : null)
                .preferredNewLecturerId(r.getPreferredNewLecturer() != null ? r.getPreferredNewLecturer().getId() : null)
                .preferredNewLecturerName(r.getPreferredNewLecturer() != null ? r.getPreferredNewLecturer().getFullName() : null)
                .mergeSessions(r.getMergeSessions().stream().map(MergeSessionDto::fromEntity)
                        .sorted((a, b) -> a.getSummary().compareTo(b.getSummary())).toList())
                .endTime(r.getEndTime())
                .roomBookingNeeded(r.getRoomBookingNeeded())
                .preferredRoomAnswer(r.getPreferredRoomAnswer())
                .changeCategory(r.getChangeCategory())
                .rationale(r.getRationale())
                .benefitToStudents(r.getBenefitToStudents())
                .academicPeriod(r.getAcademicPeriod())
                .academicYearLabel(r.getAcademicYearLabel())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    static String summariseSession(TimetableSession s) {
        return s.getDayOfWeek() + " " + s.getStartTime().toString().substring(0, 5)
                + "-" + s.getEndTime().toString().substring(0, 5) + " · " + s.getRoom().getName();
    }

    static String summariseSessionWithModule(TimetableSession s) {
        return s.getModule().getCode() + " " + summariseSession(s);
    }
}
