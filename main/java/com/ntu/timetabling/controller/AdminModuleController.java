package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.ModuleAdminDto;
import com.ntu.timetabling.dto.ModuleCreateUpdateDto;
import com.ntu.timetabling.service.AdminModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/modules")
@RequiredArgsConstructor
public class AdminModuleController {

    private final AdminModuleService adminModuleService;

    @GetMapping
    public List<ModuleAdminDto> getAll() {
        return adminModuleService.getAll();
    }

    @PostMapping
    public ResponseEntity<ModuleAdminDto> create(@Valid @RequestBody ModuleCreateUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminModuleService.create(dto));
    }

    @PutMapping("/{id}")
    public ModuleAdminDto update(@PathVariable Long id, @Valid @RequestBody ModuleCreateUpdateDto dto) {
        return adminModuleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminModuleService.delete(id);
    }
}
