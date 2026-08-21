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
    private final ChangesInQueueService changesInQueueService;

    public TimetablingTeamDashboardDto getDashboard(String username) {
        User member = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // team-wide, not scoped to this one member
        List<Request> constraintRequests = requestRepository.findByType(RequestType.CONSTRAINT);
        List<Request> changeRequests = requestRepository.findByType(RequestType.CHANGE);

        Map<String, Long> constraintStatusCounts = freshStatusMap();
        for (Request r : constraintRequests) {
            constraintStatusCounts.merge(r.getStatus().name(), 1L, Long::sum);
        }

        Map<String, Long> changeStatusCounts = freshStatusMap();
        Map<String, Long> changeCategoryCounts = new LinkedHashMap<>();
        for (Request r : changeRequests) {
            changeStatusCounts.merge(r.getStatus().name(), 1L, Long::sum);
            if (r.getChangeCategory() != null) {
                changeCategoryCounts.merge(r.getChangeCategory().name(), 1L, Long::sum);
            }
        }

        return TimetablingTeamDashboardDto.builder()
                .fullName(member.getFullName())
                .school("School of Science and Technology")
                .campus("Clifton")
                .constraintStatusCounts(constraintStatusCounts)
                .constraintTotal(constraintRequests.size())
                .changeStatusCounts(changeStatusCounts)
                .changeCategoryCounts(changeCategoryCounts)
                .changeTotal(changeRequests.size())
                .awaitingDecisionCount(constraintStatusCounts.get(RequestStatus.AWAITING_DECISION.name())
                        + changeStatusCounts.get(RequestStatus.AWAITING_DECISION.name()))
                .violationCount(violationService.getViolations().size())
                .changesInQueueCount(changesInQueueService.getChangesInQueue().size())
                .build();
    }

    private Map<String, Long> freshStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (RequestStatus s : RequestStatus.values()) {
            map.put(s.name(), 0L);
        }
        return map;
    }
}
