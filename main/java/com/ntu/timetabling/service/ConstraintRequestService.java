package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ConstraintRequestCreateDto;
import com.ntu.timetabling.dto.RequestDto;
import com.ntu.timetabling.model.ModuleEntity;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestType;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.ModuleRepository;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Increment 1: constraint submission (FR1, FR2, FR9).
 * A lecturer submits scheduling constraint preferences ahead of the main
 * annual timetable being created, marking each as a firm requirement or a
 * flexible preference, optionally against multiple modules.
 */
@Service
@RequiredArgsConstructor
public class ConstraintRequestService {

    private final RequestRepository requestRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    public RequestDto submitConstraintRequest(String username, ConstraintRequestCreateDto dto) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        Set<ModuleEntity> modules = new HashSet<>();
        for (Long moduleId : dto.getModuleIds()) {
            modules.add(moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Module not found: " + moduleId)));
        }

        Request request = Request.builder()
                .type(RequestType.CONSTRAINT)
                .requester(requester)
                .isFirm(dto.getIsFirm())
                .description(dto.getDescription())
                .modules(modules)
                .build();

        return RequestDto.fromEntity(requestRepository.save(request));
    }

    public List<RequestDto> getMyConstraintRequests(String username) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        return requestRepository.findByRequesterIdAndType(requester.getId(), RequestType.CONSTRAINT).stream()
                .map(RequestDto::fromEntity)
                .toList();
    }

    // Timetabling Team side: can view every constraint request submitted so far.
    public List<RequestDto> getAllConstraintRequests() {
        return requestRepository.findByType(RequestType.CONSTRAINT).stream()
                .map(RequestDto::fromEntity)
                .toList();
    }
}
