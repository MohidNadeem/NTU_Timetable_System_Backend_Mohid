package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Module-based constraint submission. Revised per prototype evaluation
 * feedback (14 Aug 2026):
 *   - primaryModuleId is deliberately NOT scoped to the requester's own
 *     modules - teaching assignments change year to year, so anyone should
 *     be able to submit a constraint for any module
 *   - dayOfWeek/startTime are now optional (durationHours stays required)
 *   - full week-scope choice restored
 *   - allowedRoomIds replaces a single specificRoomId - "one of these five
 *     rooms" rather than just suggesting one
 *   - groups supports one request covering several lab/seminar groups at
 *     once, each optionally naming a different intended teacher
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

    // optional - no day/time preference means "Academics decides"
    private String dayOfWeek; // MON..FRI

    // optional
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

    // optional - any of these rooms would work; empty = no specific preference
    private List<Long> allowedRoomIds;

    // defaults to NONE if not sent - optional
    private String feature;

    // optional
    private String software;

    // optional
    private String supportTeamStaff;

    @NotNull
    private Boolean lectureCapture;

    // optional
    private String note;

    // optional - covers several lab/seminar groups in one request; empty = request is just
    // about the module as a whole
    private List<RequestGroupCreateDto> groups;
}
