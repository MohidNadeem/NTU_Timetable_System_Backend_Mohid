package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Set;

/**
 * Timetabling Team's "Update Session" payload.
 *   scope = ALL_REMAINING -> updates the base session's recurring pattern directly
 *   scope = SINGLE/MULTIPLE -> creates a session_overrides row for just those week(s)
 */
@Getter
@Setter
public class UpdateSessionDto {

    @NotBlank
    private String dayOfWeek; // MON..FRI

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    // null = keep the session's current room
    private Long roomId;

    // null = keep the session's current teacher.
    private Long lecturerId;

    @NotBlank
    private String scope; // ALL_REMAINING | SINGLE | MULTIPLE

    // required when scope is SINGLE or MULTIPLE; ignored for ALL_REMAINING
    private Set<Integer> weeks;

    private Long relatedRequestId;

    private String reason;
}
