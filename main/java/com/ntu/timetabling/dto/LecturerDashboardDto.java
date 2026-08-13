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
    // status name -> count, e.g. {"AWAITING_DECISION": 3, "ACCEPTED": 1, ...}
    private Map<String, Long> requestStatusCounts;
    private long totalRequests;
}
