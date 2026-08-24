package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.ActivityLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ActivityLogDto {
    private Long id;
    private String eventType;
    private String description;
    private String actorName; // null if not meaningfully attributable
    private Long relatedSessionId;
    private Long relatedRequestId;
    private LocalDateTime createdAt;

    public static ActivityLogDto fromEntity(ActivityLog a) {
        return ActivityLogDto.builder()
                .id(a.getId())
                .eventType(a.getEventType())
                .description(a.getDescription())
                .actorName(a.getActor() != null ? a.getActor().getFullName() : null)
                .relatedSessionId(a.getRelatedSession() != null ? a.getRelatedSession().getId() : null)
                .relatedRequestId(a.getRelatedRequest() != null ? a.getRelatedRequest().getId() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
