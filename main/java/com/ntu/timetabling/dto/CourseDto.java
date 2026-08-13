package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String code;
    private String name;

    public static CourseDto fromEntity(Course c) {
        return CourseDto.builder().id(c.getId()).code(c.getCode()).name(c.getName()).build();
    }
}
