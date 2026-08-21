package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.LecturerDashboardDto;
import com.ntu.timetabling.dto.ModuleDto;
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
public class LecturerDashboardService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final TimetableService timetableService;

    public LecturerDashboardDto getDashboard(String username) {
        User lecturer = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        List<ModuleDto> teachingModules = timetableService.getModulesTaughtBy(lecturer.getId());
        List<Request> requests = requestRepository.findByRequesterIdOrderByCreatedAtDesc(lecturer.getId());

        Map<String, Long> constraintStatusCounts = freshStatusMap();
        Map<String, Long> changeStatusCounts = freshStatusMap();
        Map<String, Long> changeCategoryCounts = new LinkedHashMap<>();
        long constraintTotal = 0;
        long changeTotal = 0;

        for (Request r : requests) {
            if (r.getType() == RequestType.CONSTRAINT) {
                constraintStatusCounts.merge(r.getStatus().name(), 1L, Long::sum);
                constraintTotal++;
            } else if (r.getType() == RequestType.CHANGE) {
                changeStatusCounts.merge(r.getStatus().name(), 1L, Long::sum);
                changeTotal++;
                if (r.getChangeCategory() != null) {
                    changeCategoryCounts.merge(r.getChangeCategory().name(), 1L, Long::sum);
                }
            }
        }

        return LecturerDashboardDto.builder()
                .fullName(lecturer.getFullName())
                .school("School of Science and Technology")
                .campus("Clifton")
                .teachingModules(teachingModules)
                .constraintStatusCounts(constraintStatusCounts)
                .constraintTotal(constraintTotal)
                .changeStatusCounts(changeStatusCounts)
                .changeCategoryCounts(changeCategoryCounts)
                .changeTotal(changeTotal)
                .build();
    }

    // seeding every status at 0 first so the dashboard always shows all 6, not just the ones in use
    private Map<String, Long> freshStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (RequestStatus s : RequestStatus.values()) {
            map.put(s.name(), 0L);
        }
        return map;
    }
}
