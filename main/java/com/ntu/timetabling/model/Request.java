package com.ntu.timetabling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Increment 1 (FR1/FR2): type = CONSTRAINT, submitted ahead of the annual
 * timetable, with isFirm marking a firm requirement vs a flexible preference.
 *
 * Increment 2 (FR3-FR9): type = CHANGE, submitted against an existing
 * timetable_sessions row (session), tracked through status, and optionally
 * carrying a reason/comment (FR6) once actioned or rejected.
 */
@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Only setting it for CHANGE requests (Increment 2)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TimetableSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    // FR2: firm requirement vs flexible preference (CONSTRAINT requests only)
    @Column(name = "is_firm")
    private Boolean isFirm;

    // FR8: e.g. "pre-timetable", "in-term change"
    @Column(length = 50)
    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // FR6: set when the request is actioned/rejected, visible to the requester
    @Column(name = "reason_comment", columnDefinition = "TEXT")
    private String reasonComment;

    // FR9: a single request can be linked to multiple modules
    @ManyToMany
    @JoinTable(
            name = "request_modules",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    @Builder.Default
    private Set<ModuleEntity> modules = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
