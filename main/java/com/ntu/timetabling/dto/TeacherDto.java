package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TeacherDto {
    private Long id;
    private String fullName;

    public static TeacherDto fromEntity(User u) {
        return TeacherDto.builder().id(u.getId()).fullName(u.getFullName()).build();
    }
}
