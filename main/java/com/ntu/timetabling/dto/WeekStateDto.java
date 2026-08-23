package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

/**
 * A session's actual resolved state for one specific week
 * which day/time/room it's really at for that week,
 * accounting for any session_overrides row covering it.
 */
@Getter
@Builder
@AllArgsConstructor
public class WeekStateDto {
    private Integer week;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomName;
}
