package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ModuleDto;
import com.ntu.timetabling.model.ModuleEntity;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.repository.ModuleRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
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
    private final TimetableSessionRepository timetableSessionRepository;

    @GetMapping
    public List<ModuleDto> getAllModules() {
        // deriving each module's block from its own sessions
        return moduleRepository.findAll().stream()
                .map(this::toDtoWithBlock)
                .toList();
    }

    private ModuleDto toDtoWithBlock(ModuleEntity m) {
        Integer block = timetableSessionRepository.findFirstByModuleId(m.getId())
                .map(TimetableSession::getBlock)
                .orElse(null);
        return ModuleDto.fromEntity(m, block);
    }
}
