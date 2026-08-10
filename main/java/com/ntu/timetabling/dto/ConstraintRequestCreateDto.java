package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Input payload for FR1/FR2 - a lecturer submitting a constraint preference
 * ahead of the annual timetable being created.
 */
@Getter
@Setter
public class ConstraintRequestCreateDto {

    @NotBlank
    private String description;

    // FR2: true = firm requirement, false = flexible preference
    @NotNull
    private Boolean isFirm;

    // FR9: request can be linked to one or more modules
    @NotEmpty
    private List<Long> moduleIds;
}
