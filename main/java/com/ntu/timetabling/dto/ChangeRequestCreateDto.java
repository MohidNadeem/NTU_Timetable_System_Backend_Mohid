package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
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

    @NotNull
    private Boolean roomBookingNeeded;

    @NotBlank
    private String weekMode; // SINGLE | MULTIPLE | ALL_REMAINING

    // required when weekMode is SINGLE or MULTIPLE; ignored for ALL_REMAINING
    private Set<Integer> weeks;

    @NotNull
    private Integer block;

    // the lecturer's actual currently-scheduled session being asked about
    @NotNull
    private Long sessionId;

    // preferred new slot - all optional, since a request might only be about e.g. the room
    private String dayOfWeek; // MON..FRI
    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank
    private String deliveryType; // reuses the same learningActivity vocabulary as constraints

    @NotBlank
    private String preferredRoomAnswer; // YES | NO | ONLINE

    // required only when preferredRoomAnswer is YES
    private Long specificRoomId;

    @NotBlank
    private String changeCategory;

    @NotBlank
    private String rationale;

    @NotBlank
    private String benefitToStudents;
}
