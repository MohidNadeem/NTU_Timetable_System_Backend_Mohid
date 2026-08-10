package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.SessionType;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.Weekday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class TimetableSessionDto {
    private Long id;
    private String moduleCode;
    private String moduleName;
    private String roomName;
    private String lecturerName;
    private SessionType sessionType;
    private Weekday dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public static TimetableSessionDto fromEntity(TimetableSession s) {
        return TimetableSessionDto.builder()
                .id(s.getId())
                .moduleCode(s.getModule().getCode())
                .moduleName(s.getModule().getName())
                .roomName(s.getRoom().getName())
                .lecturerName(s.getLecturer().getFullName())
                .sessionType(s.getSessionType())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();
    }
}
