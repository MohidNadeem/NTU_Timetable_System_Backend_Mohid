package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CreateUserDto;
import com.ntu.timetabling.dto.UpdateUserDto;
import com.ntu.timetabling.dto.UserAdminDto;
import com.ntu.timetabling.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<UserAdminDto> getAll() {
        return adminUserService.getAllUsers();
    }

    @PostMapping
    public ResponseEntity<UserAdminDto> create(@Valid @RequestBody CreateUserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(dto));
    }

    @PutMapping("/{id}")
    public UserAdminDto update(@PathVariable Long id, @Valid @RequestBody UpdateUserDto dto) {
        return adminUserService.updateUser(id, dto);
    }
}
