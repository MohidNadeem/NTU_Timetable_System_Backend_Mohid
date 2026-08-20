package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Set;

/**
 * "Add Session" - creates a brand new TimetableSession to fulfil an
 * Additional Session change request.
 */
@Getter
@Setter
public class SessionCreateDto {

    @NotNull
    private Long moduleId;

    @NotNull
    private Long roomId;

    // defaults to the change request's own requester if not supplied
    private Long lecturerId;

    @NotBlank
    private String sessionType; // LECTURE | SEMINAR | LAB | TUTORIAL | SURGERY | PROJECT

    @NotBlank
    private String dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer block;

    // only set when this session fulfils a specific change/constraint request
    private Long relatedRequestId;

    // a distinguishing name/code for the session
    private String sessionLabel;

    private Set<Integer> restrictToWeeks;
}
