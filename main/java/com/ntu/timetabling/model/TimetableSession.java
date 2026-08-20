package com.ntu.timetabling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * The self-seeded, mock "live timetable" (Increment 0). Stands in for NTU's
 * real timetable feed and is the source of truth that change requests get
 * validated against later (FR4).
 */
@Entity
@Table(name = "timetable_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private User lecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Weekday dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // adding this so the timetable can be split into 26/27's 4 teaching blocks
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int block;

    // distinguishing repeated slots of the same type/module in a week (e.g. "Lecture 1" vs "Lecture 2")
    @Column(name = "part_number", columnDefinition = "TINYINT")
    private Integer partNumber;

    // using this to override the default type-based name for anything non-standard (e.g. MP's briefing/supervision)
    @Column(name = "session_label", length = 100)
    private String sessionLabel;

    // set when this session was created via the "Add Session" action
    // to fulfil an Additional Session change request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_request_id")
    private Request relatedRequest;

    // soft cancellation (Session Removal category)
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_request_id")
    private Request cancelledByRequest;

    // partial cancellation - specific weeks removed rather than the whole session
    @ElementCollection
    @CollectionTable(name = "session_cancelled_weeks", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "week_in_block", columnDefinition = "TINYINT")
    @Builder.Default
    private Set<Integer> cancelledWeeks = new HashSet<>();

    // linking which course(s)/section(s) this session is for, so combined vs split sessions can be told apart
    @ManyToMany
    @JoinTable(
            name = "session_courses",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private Set<Course> courses = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isFullyCancelled() {
        return cancelledAt != null;
    }

    public boolean isActiveInWeek(int week) {
        return cancelledAt == null && !cancelledWeeks.contains(week);
    }
}
