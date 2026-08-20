package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

// Change request submission.
@Getter
@Setter
public class ChangeRequestCreateDto {

    @NotNull
    private Long departmentId;

    @NotNull
    private Long primaryModuleId;

    @NotBlank
    private String academicPeriod; // HALF_YEAR_1 | HALF_YEAR_2 | FULL_YEAR

    @NotBlank
    private String weekMode; // SINGLE | MULTIPLE | ALL_REMAINING

    // required when weekMode is SINGLE or MULTIPLE; ignored for ALL_REMAINING
    private Set<Integer> weeks;

    @NotNull
    private Integer block;

    // the lecturer's actual currently-scheduled session being asked about
    // ADDITIONAL_SESSION (nothing exists yet) and MERGE_SESSIONS_GROUPS (uses mergeSessionIds instead)
    private Long sessionId;

    // CLASHES only - the other session this one clashes with
    private Long clashingSessionId;

    // STAFF_CHANGE only - who the lecturer would like teaching it instead
    private Long preferredNewLecturerId;

    // MERGE_SESSIONS_GROUPS only - which existing sessions to combine (2+ expected)
    private List<Long> mergeSessionIds;

    // preferred new slot - all optional, since a request might only be about e.g. the room
    private String dayOfWeek; // MON..FRI
    private LocalTime startTime;
    private LocalTime endTime;

    private String deliveryType; // reuses the same learningActivity vocabulary as constraints


    private String roomType;
    private List<Long> allowedRoomIds;

    @NotBlank
    private String changeCategory;

    // always required - the "what needs to happen" explanation
    @NotBlank
    private String rationale;

    @NotBlank
    private String benefitToStudents;
}
