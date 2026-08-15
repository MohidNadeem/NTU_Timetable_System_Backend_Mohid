package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.EffectResultDto;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.model.RequestType;
import com.ntu.timetabling.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Changes in Queue = every ACCEPTED change request whose effect isn't yet
 * applied to the schedule.
 * Kept separate from Violations (rather than merging them into one list)
 */
@Service
@RequiredArgsConstructor
public class ChangesInQueueService {

    private final RequestRepository requestRepository;
    private final EffectService effectService;

    public List<EffectResultDto> getChangesInQueue() {
        return requestRepository.findFiltered(RequestType.CHANGE, null, null, RequestStatus.ACCEPTED, null, null)
                .stream()
                .map(effectService::computeEffect)
                .filter(e -> !e.isSatisfied())
                .toList();
    }
}
