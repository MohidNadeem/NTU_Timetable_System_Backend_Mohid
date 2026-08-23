package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CommentDto;
import com.ntu.timetabling.dto.CreateCommentDto;
import com.ntu.timetabling.service.RequestCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// shared between lecturer and Timetabling Team
@RestController
@RequestMapping("/api/requests/{requestId}/comments")
@RequiredArgsConstructor
public class RequestCommentController {

    private final RequestCommentService requestCommentService;

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long requestId, Authentication authentication) {
        return requestCommentService.getComments(requestId, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable Long requestId,
                                                  @Valid @RequestBody CreateCommentDto dto,
                                                  Authentication authentication) {
        CommentDto created = requestCommentService.addComment(requestId, authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
