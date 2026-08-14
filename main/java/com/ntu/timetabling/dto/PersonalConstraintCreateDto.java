package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

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

    // optional - day(s) of the week the lecturer is unavailable
    private Set<String> unavailableDays;

    // optional - date range this applies to
    private LocalDate unavailableFromDate;
    private LocalDate unavailableToDate;

    // optional - time-of-day window; leave both blank for "all day"
    private LocalTime unavailableFromTime;
    private LocalTime unavailableToTime;
}
