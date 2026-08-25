package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.CommentDto;
import com.ntu.timetabling.model.CommentAttachment;
import com.ntu.timetabling.service.RequestCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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

    // multipart, not JSON - comment text and an optional file arrive together in one request,
    // rather than a two-step "create comment, then attach file" that could leave a comment
    // sitting with a half-failed attachment attempt
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentDto> addComment(@PathVariable Long requestId,
                                                  @RequestParam("comment") String comment,
                                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                                  Authentication authentication) {
        CommentDto created = requestCommentService.addComment(requestId, authentication.getName(), comment, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long requestId,
                                                       @PathVariable Long attachmentId,
                                                       Authentication authentication) {
        CommentAttachment attachment = requestCommentService.downloadAttachment(requestId, attachmentId, authentication.getName());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(attachment.getFileData());
    }
}
