package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Set;

/**
 * Module-based constraint submission.
 * The fields marked required in the elicitation sheet are @NotNull/@NotBlank
 * everything else is optional.
 */
@Getter
@Setter
public class ModuleConstraintCreateDto {

    @NotNull
    private Long departmentId;

    @NotNull
    private Long primaryModuleId;

    // optional
    private Long linkedModuleId;

    // optional
    private String additionalLinkedModules;

    @NotNull
    private Integer block;

    @NotBlank
    private String weekMode; // ALL_REMAINING | SINGLE | MULTIPLE

    // required when weekMode is SINGLE (1 entry) or MULTIPLE (several entries); ignored for ALL_REMAINING
    private Set<Integer> weeks;

    @NotBlank
    private String dayOfWeek; // MON..FRI

    @NotNull
    private LocalTime startTime;

    @NotNull
    private Integer durationHours; // 1 or 2

    @NotBlank
    private String learningActivity;

    // optional
    private String personalTutorDetail;

    // optional
    private String activityDetail;

    // optional
    private String titleTechnical;

    @NotBlank
    private String roomType; // OFFSITE | POOLED | RESTRICTED | ONLINE | NO_ROOM_REQ

    // defaults to NONE if not sent
    private String preferredRoomLayout;

    private Long specificRoomId;

    // defaults to NONE if not sent
    private String feature;

    // optional
    private String software;

    // optional
    private String supportTeamStaff;

    @NotNull
    private Boolean lectureCapture;

    // optional
    private String note;
}
