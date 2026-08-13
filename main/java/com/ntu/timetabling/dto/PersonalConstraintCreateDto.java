package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Personal constraint submission - department, campus, and the lecturer's own explanation + reason.
// staff comes from the logged-in user, campus is fixed.
@Getter
@Setter
public class PersonalConstraintCreateDto {

    @NotNull
    private Long departmentId;

    @NotBlank
    private String description; // "Explain your constraint"

    @NotBlank
    private String reason;
}
