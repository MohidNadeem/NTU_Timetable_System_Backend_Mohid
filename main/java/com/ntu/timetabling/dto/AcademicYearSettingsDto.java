package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AcademicYearSettingsDto {
    private String currentYearLabel;
}
