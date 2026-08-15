package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.AcademicYearSettingsDto;
import com.ntu.timetabling.dto.UpdateAcademicYearDto;
import com.ntu.timetabling.model.AcademicYearSettings;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.AcademicYearSettingsRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearSettingsRepository academicYearSettingsRepository;
    private final UserRepository userRepository;

    public AcademicYearSettingsDto getCurrent() {
        return AcademicYearSettingsDto.builder()
                .currentYearLabel(getSettingsRow().getCurrentYearLabel())
                .build();
    }

    public AcademicYearSettingsDto updateCurrent(UpdateAcademicYearDto dto, String actingUsername) {
        User actingUser = userRepository.findByUsername(actingUsername)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        AcademicYearSettings settings = getSettingsRow();
        settings.setCurrentYearLabel(dto.getCurrentYearLabel());
        settings.setUpdatedBy(actingUser);
        academicYearSettingsRepository.save(settings);

        return AcademicYearSettingsDto.builder().currentYearLabel(settings.getCurrentYearLabel()).build();
    }

    private AcademicYearSettings getSettingsRow() {
        return academicYearSettingsRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Academic year settings not configured"));
    }
}
