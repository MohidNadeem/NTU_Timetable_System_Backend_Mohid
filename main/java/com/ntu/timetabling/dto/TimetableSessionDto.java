package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.SessionType;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.Weekday;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class TimetableSessionDto {
    private Long id;
    private String moduleCode;
    private String moduleName;
    private String roomName;
    private String roomBuilding;
    private String lecturerName;
    private SessionType sessionType;
    private Weekday dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int block;
    private Integer partNumber;
    private String sessionLabel;
    // which course(s)/section(s) this session is for, e.g. ["CS","CC"] for a combined lecture
    private List<String> courseCodes;
    // true when this row's day/time/room reflect a week-specific override rather than the base recurring pattern
    @Builder.Default
    private boolean isOverridden = false;

    public static TimetableSessionDto fromEntity(TimetableSession s) {
        return TimetableSessionDto.builder()
                .id(s.getId())
                .moduleCode(s.getModule().getCode())
                .moduleName(s.getModule().getName())
                .roomName(s.getRoom().getName())
                .roomBuilding(s.getRoom().getBuilding())
                .lecturerName(s.getLecturer().getFullName())
                .sessionType(s.getSessionType())
                .dayOfWeek(s.getDayOfWeek())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .block(s.getBlock())
                .partNumber(s.getPartNumber())
                .sessionLabel(s.getSessionLabel())
                .courseCodes(s.getCourses().stream()
                        .map(c -> c.getCode())
                        .sorted()
                        .collect(Collectors.toList()))
                .build();
    }
}
