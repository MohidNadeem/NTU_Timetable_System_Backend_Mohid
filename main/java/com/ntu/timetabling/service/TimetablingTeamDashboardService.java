package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.TimetablingTeamDashboardDto;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.model.RequestType;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimetablingTeamDashboardService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ViolationService violationService;

    public TimetablingTeamDashboardDto getDashboard(String username) {
        User member = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // team-wide, not scoped to this one member
        List<Request> requests = requestRepository.findByType(RequestType.CONSTRAINT);

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (RequestStatus s : RequestStatus.values()) {
            statusCounts.put(s.name(), 0L);
        }
        for (Request r : requests) {
            statusCounts.merge(r.getStatus().name(), 1L, Long::sum);
        }

        return TimetablingTeamDashboardDto.builder()
                .fullName(member.getFullName())
                .school("School of Science and Technology")
                .campus("Clifton")
                .requestStatusCounts(statusCounts)
                .totalRequests(requests.size())
                .awaitingDecisionCount(statusCounts.get(RequestStatus.AWAITING_DECISION.name()))
                .violationCount(violationService.getViolations().size())
                .build();
    }
}
