package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequestStatusDto {

    @NotBlank
    private String status;

    private String reasonComment;
}
