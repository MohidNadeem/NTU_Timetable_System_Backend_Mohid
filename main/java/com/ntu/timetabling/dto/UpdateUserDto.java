package com.ntu.timetabling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    private String accountStatus; // ACTIVE | LEAVER | ALUMNI

    // students only
    private Long courseId;
    private String groupLabel;
}
