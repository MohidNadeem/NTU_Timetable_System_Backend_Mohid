package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class TimetablingTeamDashboardDto {
    private String fullName;
    private String school;
    private String campus;
    // status name -> count, across every lecturer's constraint requests
    private Map<String, Long> requestStatusCounts;
    private long totalRequests;
    private long awaitingDecisionCount;
    private long violationCount;
}
