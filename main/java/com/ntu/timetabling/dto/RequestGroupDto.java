package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.RequestGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RequestGroupDto {
    private Long id;
    private String groupLabel;
    private Long preferredLecturerId;
    private String preferredLecturerName;

    public static RequestGroupDto fromEntity(RequestGroup g) {
        return RequestGroupDto.builder()
                .id(g.getId())
                .groupLabel(g.getGroupLabel())
                .preferredLecturerId(g.getPreferredLecturer() != null ? g.getPreferredLecturer().getId() : null)
                .preferredLecturerName(g.getPreferredLecturer() != null ? g.getPreferredLecturer().getFullName() : null)
                .build();
    }
}
