package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

/**
 * One row on the Violations page: an ACCEPTED module-based constraint whose
 * requested day/time/room doesn't match what's actually scheduled for that module right now.
 */
@Getter
@Builder
@AllArgsConstructor
public class ViolationDto {
    private Long requestId;
    private String requesterName;
    private String departmentCode;
    private String primaryModuleCode;
    private String primaryModuleName;
    private Integer block;
    private String learningActivity;

    // what the lecturer asked for
    private String requestedDayOfWeek;
    private LocalTime requestedStartTime;
    private Integer requestedDurationHours;
    private String requestedRoomName; // null if no specific room was requested

    // what's actually scheduled right now (null fields if hasMatchedSession = false)
    private boolean hasMatchedSession;
    private Long matchedSessionId;
    private String matchedSessionType;
    private String currentDayOfWeek;
    private LocalTime currentStartTime;
    private LocalTime currentEndTime;
    private String currentRoomName;
}
