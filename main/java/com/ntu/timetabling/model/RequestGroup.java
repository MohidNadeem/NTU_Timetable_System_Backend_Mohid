package com.ntu.timetabling.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One lab/seminar group within a module-based constraint request that covers several groups at once
 * Day/time/duration/room stay at the Request level, shared across every group
 * only the label and preferred teacher differ per group.
 */
@Entity
@Table(name = "request_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(name = "group_label", nullable = false, length = 100)
    private String groupLabel;

    // optional - who the lecturer wants to lead this specific group, if known
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_lecturer_id")
    private User preferredLecturer;
}
