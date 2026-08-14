package com.ntu.timetabling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Increment 1 (FR1/FR2): type = CONSTRAINT, split into two types
 * MODULE
 * PERSONAL
 *
 * Increment 2 (FR3-FR9): type = CHANGE, submitted against an existing
 * timetable_sessions row (session).
 *
 * status starts at AWAITING_DECISION on submission and can only move
 * forward via the Timetabling Team - lecturers can never set it themselves.
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

    // only set when type = CONSTRAINT; distinguishes the two constraint forms
    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_kind")
    private ConstraintKind constraintKind;

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
    private RequestStatus status = RequestStatus.AWAITING_DECISION;

    // FR2: firm requirement vs flexible preference (CONSTRAINT requests only)
    @Column(name = "is_firm")
    private Boolean isFirm;

    // FR8: e.g. "pre-timetable", "in-term change"
    @Column(length = 50)
    private String category;

    // PERSONAL: "explain your constraint".
    @Column(columnDefinition = "TEXT")
    private String description;

    // PERSONAL only: the lecturer's own stated reason for the constraint
    @Column(columnDefinition = "TEXT")
    private String reason;

    // set by the Timetabling Team when actioned/rejected (any type)
    @Column(name = "reason_comment", columnDefinition = "TEXT")
    private String reasonComment;

    // ---- MODULE-based constraint fields -----

    // doubles as "Department" for both MODULE and PERSONAL kinds
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Course department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_module_id")
    private ModuleEntity primaryModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_module_id")
    private ModuleEntity linkedModule;

    @Column(name = "additional_linked_modules", columnDefinition = "TEXT")
    private String additionalLinkedModules;

    @Column(columnDefinition = "TINYINT")
    private Integer block;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_mode")
    private WeekMode weekMode;

    // only populated when weekMode = SINGLE or MULTIPLE; ALL_REMAINING needs no rows here
    @ElementCollection
    @CollectionTable(name = "request_weeks", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "week_in_block", columnDefinition = "TINYINT")
    @Builder.Default
    private Set<Integer> weeks = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Weekday dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "duration_hours", columnDefinition = "TINYINT")
    private Integer durationHours;

    @Column(name = "learning_activity", length = 100)
    private String learningActivity;

    @Column(name = "personal_tutor_detail", length = 255)
    private String personalTutorDetail;

    @Column(name = "activity_detail", columnDefinition = "TEXT")
    private String activityDetail;

    @Column(name = "title_technical", length = 255)
    private String titleTechnical;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String campus = "Clifton";

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_room_layout")
    @Builder.Default
    private RoomLayout preferredRoomLayout = RoomLayout.NONE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specific_room_id")
    private Room specificRoom;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomFeature feature = RoomFeature.NONE;

    @Column(length = 255)
    private String software;

    @Column(name = "support_team_staff", length = 255)
    private String supportTeamStaff;

    @Column(name = "lecture_capture")
    private Boolean lectureCapture;

    @Column(columnDefinition = "TEXT")
    private String note;

    // ---- PERSONAL-constraint fields ------

    // which day(s) of the week the lecturer is unavailable
    @ElementCollection
    @CollectionTable(name = "request_unavailable_days", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Weekday> unavailableDays = new HashSet<>();

    @Column(name = "unavailable_from_date")
    private LocalDate unavailableFromDate;

    @Column(name = "unavailable_to_date")
    private LocalDate unavailableToDate;

    @Column(name = "unavailable_from_time")
    private LocalTime unavailableFromTime;

    @Column(name = "unavailable_to_time")
    private LocalTime unavailableToTime;

    // ------------------------------------------------------------------------

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
