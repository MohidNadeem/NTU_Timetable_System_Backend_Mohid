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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Increment 1 (FR1/FR2): type = CONSTRAINT, split into two kinds via
 * constraintKind:
 *   MODULE   - the rich structured form (department, module, week/day/time,
 *              room preferences, etc.) - most fields below apply only here
 *   PERSONAL - a lightweight "I don't want X" request - only uses
 *              department, campus, description ("explain your constraint")
 *              and reason
 *
 * Increment 2 (FR3-FR9): type = CHANGE, submitted against an existing
 * timetable_sessions row (session).
 *
 * status starts at AWAITING_DECISION on submission and can only move
 * forward via the Timetabling Team - lecturers never set it themselves.
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

    // only set when type = CONSTRAINT - distinguishes the two constraint forms
    @Enumerated(EnumType.STRING)
    @Column(name = "constraint_kind")
    private ConstraintKind constraintKind;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // Only set for CHANGE requests (Increment 2)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TimetableSession session;

    // CLASHES category only - the other session this one clashes with (session above is the
    // lecturer's own)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clashing_session_id")
    private TimetableSession clashingSession;

    // STAFF_CHANGE category only - who the lecturer would like teaching it instead
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_new_lecturer_id")
    private User preferredNewLecturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.AWAITING_DECISION;

    // FR2: firm requirement vs flexible preference (legacy, CONSTRAINT requests only)
    @Column(name = "is_firm")
    private Boolean isFirm;

    // FR8: e.g. "pre-timetable", "in-term change"
    @Column(length = 50)
    private String category;

    // PERSONAL: "explain your constraint". MODULE: unused - structured fields carry the info.
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

    // several acceptable rooms rather than pinning to one specific room
    @ManyToMany
    @JoinTable(
            name = "request_rooms",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    @Builder.Default
    private Set<Room> allowedRooms = new HashSet<>();

    // CHANGE requests, "Merge sessions/groups" category
    // which existing sessions the lecturer wants combined into one.
    // Empty for every other category/kind.
    @ManyToMany
    @JoinTable(
            name = "request_merge_sessions",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "session_id")
    )
    @Builder.Default
    private Set<TimetableSession> mergeSessions = new HashSet<>();

    // module-based constraints: covers several lab/seminar groups in one request
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RequestGroup> groups = new ArrayList<>();

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

    // ---- CHANGE-request fields (Increment 2) ------
    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room_booking_needed")
    private Boolean roomBookingNeeded;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_room_answer")
    private PreferredRoomAnswer preferredRoomAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_category")
    private ChangeCategory changeCategory;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "benefit_to_students", columnDefinition = "TEXT")
    private String benefitToStudents;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_period")
    private AcademicPeriod academicPeriod;

    // the academic year label at submission time
    @Column(name = "academic_year_label", length = 20)
    private String academicYearLabel;

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
