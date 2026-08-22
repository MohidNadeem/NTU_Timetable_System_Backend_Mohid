package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseCreateUpdateDto {

    @NotBlank
    private String code;

    @NotBlank
    private String name;
}
