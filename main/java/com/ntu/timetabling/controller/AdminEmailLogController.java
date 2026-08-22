package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.EmailLogDto;
import com.ntu.timetabling.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Simple audit view for Admin
// every attempted email (sent or not) is logged here regardless
@RestController
@RequestMapping("/api/admin/email-log")
@RequiredArgsConstructor
public class AdminEmailLogController {

    private final EmailLogRepository emailLogRepository;

    @GetMapping
    public List<EmailLogDto> getAll() {
        return emailLogRepository.findAll(Sort.by(Sort.Direction.DESC, "sentAt"))
                .stream().map(EmailLogDto::fromEntity).toList();
    }
}
