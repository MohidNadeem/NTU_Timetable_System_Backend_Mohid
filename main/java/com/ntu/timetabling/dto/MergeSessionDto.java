package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.TimetableSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MergeSessionDto {
    private Long id;
    private String summary; // e.g. "CS4002 MON 14:00-16:00 · MAE206"

    public static MergeSessionDto fromEntity(TimetableSession s) {
        return MergeSessionDto.builder()
                .id(s.getId())
                .summary(RequestDto.summariseSessionWithModule(s))
                .build();
    }
}
