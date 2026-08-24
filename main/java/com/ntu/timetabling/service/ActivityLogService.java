package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ActivityLogDto;
import com.ntu.timetabling.model.ActivityLog;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.ActivityLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Increment 4b - a record of session and request changes, scoped per role at query time.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public void log(String eventType, String description, User actor, User affectedLecturer,
                     TimetableSession relatedSession, Request relatedRequest) {
        activityLogRepository.save(ActivityLog.builder()
                .eventType(eventType)
                .description(description)
                .actor(actor)
                .affectedLecturer(affectedLecturer)
                .relatedSession(relatedSession)
                .relatedRequest(relatedRequest)
                .build());
    }

    public List<ActivityLogDto> getForTeam() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ActivityLogDto::fromEntity).toList();
    }

    public List<ActivityLogDto> getForLecturer(Long lecturerId) {
        return activityLogRepository.findByActorIdOrAffectedLecturerIdOrderByCreatedAtDesc(lecturerId, lecturerId)
                .stream().map(ActivityLogDto::fromEntity).toList();
    }

    // session-only events for the student's own course
    // further narrowed to their group where the session names one
    public List<ActivityLogDto> getForStudent(User student) {
        if (student.getCourse() == null) {
            return List.of();
        }
        return activityLogRepository.findSessionEventsForCourse(student.getCourse().getId()).stream()
                .filter(a -> GroupLabelUtil.isRelevantToStudent(
                        a.getRelatedSession() != null ? a.getRelatedSession().getSessionLabel() : null,
                        student.getGroupLabel()))
                .map(ActivityLogDto::fromEntity)
                .toList();
    }
}
