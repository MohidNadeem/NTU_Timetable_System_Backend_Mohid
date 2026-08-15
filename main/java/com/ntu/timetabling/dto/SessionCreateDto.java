package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

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

    @NotNull
    private Long relatedRequestId;
}
