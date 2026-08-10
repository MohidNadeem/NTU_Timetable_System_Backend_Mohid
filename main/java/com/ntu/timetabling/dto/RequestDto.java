package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestStatus;
import com.ntu.timetabling.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private RequestType type;
    private String requesterName;
    private RequestStatus status;
    private Boolean isFirm;
    private String category;
    private String description;
    private String reasonComment;
    private List<String> moduleCodes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RequestDto fromEntity(Request r) {
        return RequestDto.builder()
                .id(r.getId())
                .type(r.getType())
                .requesterName(r.getRequester().getFullName())
                .status(r.getStatus())
                .isFirm(r.getIsFirm())
                .category(r.getCategory())
                .description(r.getDescription())
                .reasonComment(r.getReasonComment())
                .moduleCodes(r.getModules().stream()
                        .map(m -> m.getCode())
                        .collect(Collectors.toList()))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
