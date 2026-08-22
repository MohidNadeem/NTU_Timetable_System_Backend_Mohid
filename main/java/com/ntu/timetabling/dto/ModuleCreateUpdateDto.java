package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleCreateUpdateDto {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    // which course(s) offer this module - can be empty (a module not yet assigned to any course)
    private List<Long> courseIds;
}
