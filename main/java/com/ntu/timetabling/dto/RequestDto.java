package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private RequestType type;
    private ConstraintKind constraintKind;
    private String requesterName;
    private RequestStatus status;

    // legacy / shared
    private Boolean isFirm;
    private String category;
    private String description;
    private String reason;
    private String reasonComment;

    // module-based fields (null for PERSONAL requests)
    private String departmentCode;
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
    private String specificRoomName;
    private RoomFeature feature;
    private String software;
    private String supportTeamStaff;
    private Boolean lectureCapture;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .type(r.getType())
                .constraintKind(r.getConstraintKind())
                .requesterName(r.getRequester().getFullName())
                .status(r.getStatus())
                .isFirm(r.getIsFirm())
                .category(r.getCategory())
                .description(r.getDescription())
                .reason(r.getReason())
                .reasonComment(r.getReasonComment())
                .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null)
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
                .specificRoomName(r.getSpecificRoom() != null ? r.getSpecificRoom().getName() : null)
                .feature(r.getFeature())
                .software(r.getSoftware())
                .supportTeamStaff(r.getSupportTeamStaff())
                .lectureCapture(r.getLectureCapture())
                .note(r.getNote())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
