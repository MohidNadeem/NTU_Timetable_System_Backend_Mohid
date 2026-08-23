package com.ntu.timetabling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    private String role; // ADMIN | LECTURER | TIMETABLING_TEAM | STUDENT

    // required when role = STUDENT
    private Long courseId;

    // optional - which lab/seminar group they're in
    private String groupLabel;
}
