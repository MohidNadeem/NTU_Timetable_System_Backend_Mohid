package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String fullName;
    private Role role;
    private boolean mustChangePassword;
    // students only - lets the frontend default their Timetable filter to their own course,
    // the same way a lecturer's already defaults to their own sessions
    private Long courseId;
}
