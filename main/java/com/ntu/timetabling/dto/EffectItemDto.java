package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

/**
 * MODULE constraint or CHANGE (non-additional) has 0 or 1 of these unless
 * groups are involved, in which case there's one per group.
 *
 * A PERSONAL constraint can have several (one per conflicting session).
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class EffectItemDto {
    private Long sessionId;
    private String moduleCode;
    private String moduleName;
    private String sessionType;
    private ActionType actionType;

    // set when this item represents one group within a multi-group constraint request
    private String groupLabel;
    private Long preferredLecturerId;
    private String preferredLecturerName;

    // what's actually scheduled right now (null for ADD_SESSION items)
    private String currentDayOfWeek;
    private LocalTime currentStartTime;
    private LocalTime currentEndTime;
    private String currentRoomName;

    // what was requested
    private String requestedDayOfWeek;
    private LocalTime requestedStartTime;
    private LocalTime requestedEndTime;
    private String requestedRoomName;

    // only populated for Change requests with Single/Multiple week scope
    private List<Integer> unmatchedWeeks;
}
