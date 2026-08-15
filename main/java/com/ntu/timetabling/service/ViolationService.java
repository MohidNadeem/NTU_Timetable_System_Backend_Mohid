package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.EffectResultDto;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.model.RequestType;
import com.ntu.timetabling.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Not doing full timetable generation here - instead,
 * this scans every ACCEPTED module-based constraint and checks whether
 * the timetable still actually matches what was agreed.
 * Anything that doesn't match shows up on the Violations page for the Timetabling Team to fix.
 */
@Service
@RequiredArgsConstructor
public class ViolationService {

    private final RequestRepository requestRepository;
    private final EffectService effectService;

    public List<EffectResultDto> getViolations() {
        return requestRepository.findFiltered(RequestType.CONSTRAINT, null, null, RequestStatus.ACCEPTED, null, null)
                .stream()
                .map(effectService::computeEffect)
                .filter(e -> !e.isSatisfied())
                .toList();
    }
}
