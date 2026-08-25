package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.CommentAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// metadata only
// the raw file bytes are never included here,
// they come back separately via the dedicated download endpoint (GET .../attachments/{id})
@Getter
@Builder
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String contentType;
    private long fileSize;

    public static AttachmentDto fromEntity(CommentAttachment a) {
        return AttachmentDto.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .contentType(a.getContentType())
                .fileSize(a.getFileSize())
                .build();
    }
}
