package com.ntu.timetabling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A permanent (historical) record of a session or request state change - Increment 4b.
 * Scoped per role at query time (see ActivityLogService):
 * Timetabling Team sees everything,
 * a Lecturer sees events where they're the actor or the affected teacher
 * a Student sees session-only events for their own course/group.
 */
@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_lecturer_id")
    private User affectedLecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_session_id")
    private TimetableSession relatedSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_request_id")
    private Request relatedRequest;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
