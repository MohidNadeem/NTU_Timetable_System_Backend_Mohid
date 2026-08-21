package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class LecturerDashboardDto {
    private String fullName;
    private String school;   // fixed: "School of Science and Technology" (the set scope)
    private String campus;   // fixed: "Clifton" (the set scope)
    private List<ModuleDto> teachingModules;

    // Constraint requests only
    private Map<String, Long> constraintStatusCounts;
    private long constraintTotal;

    // Change requests only - by status (same as constraints) and by category
    private Map<String, Long> changeStatusCounts;
    private Map<String, Long> changeCategoryCounts;
    private long changeTotal;
}
