package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAcademicYearDto {

    @NotBlank
    private String currentYearLabel; // e.g. "2026/27"
}
