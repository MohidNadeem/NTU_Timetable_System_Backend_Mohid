package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String type;
    private String message;
    private Long relatedRequestId;
    private Long relatedSessionId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationDto fromEntity(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .relatedRequestId(n.getRelatedRequest() != null ? n.getRelatedRequest().getId() : null)
                .relatedSessionId(n.getRelatedSession() != null ? n.getRelatedSession().getId() : null)
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
