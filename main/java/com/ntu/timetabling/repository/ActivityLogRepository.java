package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    // a Lecturer sees events where they performed the action OR
    // the event concerns a session/request that's theirs (e.g. TT updated one of their sessions)
    List<ActivityLog> findByActorIdOrAffectedLecturerIdOrderByCreatedAtDesc(Long actorId, Long affectedLecturerId);

    // every SESSION-type event (created/updated/cancelled) for sessions tagged to the given course
    @Query("SELECT DISTINCT a FROM ActivityLog a JOIN a.relatedSession s JOIN s.courses c " +
           "WHERE c.id = :courseId AND a.eventType IN ('SESSION_CREATED', 'SESSION_UPDATED', 'SESSION_CANCELLED') " +
           "ORDER BY a.createdAt DESC")
    List<ActivityLog> findSessionEventsForCourse(@Param("courseId") Long courseId);
}
