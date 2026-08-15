package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestGroupCreateDto {

    @NotBlank
    private String groupLabel;

    // optional - which existing user (LECTURER role) should lead this group, if known
    private Long preferredLecturerId;
}
