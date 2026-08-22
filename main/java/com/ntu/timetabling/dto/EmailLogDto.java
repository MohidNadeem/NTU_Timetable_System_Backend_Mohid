package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.EmailLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EmailLogDto {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String body;
    private String relatedCourseCode;
    private String status;
    private String errorMessage;
    private LocalDateTime sentAt;

    public static EmailLogDto fromEntity(EmailLog e) {
        return EmailLogDto.builder()
                .id(e.getId())
                .recipientEmail(e.getRecipientEmail())
                .subject(e.getSubject())
                .body(e.getBody())
                .relatedCourseCode(e.getRelatedCourse() != null ? e.getRelatedCourse().getCode() : null)
                .status(e.getStatus().name())
                .errorMessage(e.getErrorMessage())
                .sentAt(e.getSentAt())
                .build();
    }
}
