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
    private Map<String, Long> constraintStatusCounts;
    private long constraintTotal;
    // status/category name -> count, across every lecturer's change requests
    private Map<String, Long> changeStatusCounts;
    private Map<String, Long> changeCategoryCounts;
    private long changeTotal;
    private long awaitingDecisionCount;
    private long violationCount;
    private long changesInQueueCount;
}
