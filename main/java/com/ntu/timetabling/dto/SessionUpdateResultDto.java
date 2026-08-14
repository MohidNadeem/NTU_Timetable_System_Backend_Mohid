package com.ntu.timetabling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SessionUpdateResultDto {
    private Long sessionId;
    private String scope;
    // true = the base recurring session will be changed directly (ALL_REMAINING);
    // false = a session_overrides row will be created instead (SINGLE/MULTIPLE)
    private boolean appliedToBasePattern;
    private Long overrideId;
}
