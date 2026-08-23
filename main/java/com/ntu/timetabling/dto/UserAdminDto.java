package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserAdminDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String accountStatus;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String groupLabel;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;

    public static UserAdminDto fromEntity(User u) {
        return UserAdminDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole().name())
                .accountStatus(u.getAccountStatus().name())
                .courseId(u.getCourse() != null ? u.getCourse().getId() : null)
                .courseCode(u.getCourse() != null ? u.getCourse().getCode() : null)
                .courseName(u.getCourse() != null ? u.getCourse().getName() : null)
                .groupLabel(u.getGroupLabel())
                .mustChangePassword(u.isMustChangePassword())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
