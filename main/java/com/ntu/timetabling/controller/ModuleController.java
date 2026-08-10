package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ModuleDto;
import com.ntu.timetabling.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only module lookup, available to any authenticated user - used by the
 * lecturer-side constraint request form (FR9: link a request to modules).
 */
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;

    @GetMapping
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAll().stream()
                .map(ModuleDto::fromEntity)
                .toList();
    }
}
