package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.AcademicYearSettingsDto;
import com.ntu.timetabling.dto.UpdateAcademicYearDto;
import com.ntu.timetabling.service.AcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * GET is available to any authenticated user
 * PUT is Timetabling-Team-only via SecurityConfig's
*/
@RestController
@RequiredArgsConstructor
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping("/api/academic-year")
    public AcademicYearSettingsDto getCurrent() {
        return academicYearService.getCurrent();
    }

    @PutMapping("/api/timetabling-team/academic-year")
    public AcademicYearSettingsDto updateCurrent(@Valid @RequestBody UpdateAcademicYearDto dto,
                                                  Authentication authentication) {
        return academicYearService.updateCurrent(dto, authentication.getName());
    }
}
