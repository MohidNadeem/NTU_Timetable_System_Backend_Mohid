package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ModuleAdminDto;
import com.ntu.timetabling.dto.ModuleCreateUpdateDto;
import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.model.ModuleEntity;
import com.ntu.timetabling.repository.CourseRepository;
import com.ntu.timetabling.repository.ModuleRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminModuleService {

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final TimetableSessionRepository timetableSessionRepository;

    public List<ModuleAdminDto> getAll() {
        return moduleRepository.findAll().stream().map(ModuleAdminDto::fromEntity).toList();
    }

    public ModuleAdminDto create(ModuleCreateUpdateDto dto) {
        ModuleEntity module = ModuleEntity.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .courses(resolveCourses(dto.getCourseIds()))
                .build();
        return ModuleAdminDto.fromEntity(moduleRepository.save(module));
    }

    public ModuleAdminDto update(Long id, ModuleCreateUpdateDto dto) {
        ModuleEntity module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + id));
        module.setCode(dto.getCode());
        module.setName(dto.getName());
        module.setCourses(resolveCourses(dto.getCourseIds()));
        return ModuleAdminDto.fromEntity(moduleRepository.save(module));
    }

    public void delete(Long id) {
        ModuleEntity module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Module not found: " + id));
        // guarding against orphaning scheduled sessions - a module with real timetable sessions
        // shouldn't be deletable outright (those sessions would be left with a dangling module_id)
        if (!timetableSessionRepository.findByModuleId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    module.getCode() + " has scheduled sessions on the timetable - remove those first.");
        }
        moduleRepository.delete(module);
    }

    private Set<Course> resolveCourses(List<Long> courseIds) {
        if (courseIds == null) return new HashSet<>();
        Set<Course> courses = new HashSet<>();
        for (Long id : courseIds) {
            courses.add(courseRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Course not found: " + id)));
        }
        return courses;
    }
}
