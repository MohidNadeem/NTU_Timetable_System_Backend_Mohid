package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.AcademicYearSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearSettingsRepository extends JpaRepository<AcademicYearSettings, Long> {
}
