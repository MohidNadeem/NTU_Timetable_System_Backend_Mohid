package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.RequestComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String authorName;
    private String authorRole; // LECTURER | TIMETABLING_TEAM | ADMIN - lets the frontend style "them" vs "us" distinctly
    private String comment;
    private LocalDateTime createdAt;

    public static CommentDto fromEntity(RequestComment c) {
        return CommentDto.builder()
                .id(c.getId())
                .authorName(c.getAuthor().getFullName())
                .authorRole(c.getAuthor().getRole().name())
                .comment(c.getComment())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
