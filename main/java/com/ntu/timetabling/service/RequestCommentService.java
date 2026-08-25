package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.AttachmentDto;
import com.ntu.timetabling.dto.CommentDto;
import com.ntu.timetabling.model.CommentAttachment;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestComment;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.CommentAttachmentRepository;
import com.ntu.timetabling.repository.RequestCommentRepository;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * The "2-way communication" thread on a request
 * Adding an Extra Increment 5: File attachment (Future Work)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RequestCommentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg"
    );
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    private final RequestCommentRepository requestCommentRepository;
    private final CommentAttachmentRepository commentAttachmentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<CommentDto> getComments(Long requestId, String username) {
        Request request = findRequest(requestId);
        User user = findUser(username);
        assertCanAccess(request, user);

        return requestCommentRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(c -> CommentDto.fromEntity(c, findAttachmentDto(c.getId())))
                .toList();
    }

    public CommentDto addComment(Long requestId, String username, String commentText, MultipartFile file) {
        Request request = findRequest(requestId);
        User author = findUser(username);
        assertCanAccess(request, author);

        if (file != null && !file.isEmpty()) {
            validateFile(file);
        }

        RequestComment saved = requestCommentRepository.save(RequestComment.builder()
                .request(request)
                .author(author)
                .comment(commentText)
                .build());

        AttachmentDto attachmentDto = null;
        if (file != null && !file.isEmpty()) {
            attachmentDto = saveAttachment(saved, file);
        }

        notifyOtherParty(request, author);

        return CommentDto.fromEntity(saved, attachmentDto);
    }

    // Streams the raw file back - permission-checked via the attachment's parent request,
    // same requester-or-Timetabling-Team rule as viewing the comment thread itself.
    public CommentAttachment downloadAttachment(Long requestId, Long attachmentId, String username) {
        Request request = findRequest(requestId);
        User user = findUser(username);
        assertCanAccess(request, user);

        CommentAttachment attachment = commentAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found: " + attachmentId));

        if (!attachment.getComment().getRequest().getId().equals(requestId)) {
            throw new AccessDeniedException("This attachment does not belong to that request");
        }

        return attachment;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File is too large - the limit is 10MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type - only PDF, DOC, DOCX, PNG, and JPEG are allowed");
        }
    }

    private AttachmentDto saveAttachment(RequestComment comment, MultipartFile file) {
        try {
            CommentAttachment attachment = commentAttachmentRepository.save(CommentAttachment.builder()
                    .comment(comment)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileData(file.getBytes())
                    .build());
            return AttachmentDto.fromEntity(attachment);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded file - please try again");
        }
    }

    private AttachmentDto findAttachmentDto(Long commentId) {
        return commentAttachmentRepository.findByCommentId(commentId)
                .map(AttachmentDto::fromEntity)
                .orElse(null);
    }

    // the requester's own comment notifies the whole Timetabling Team
    // a TT member's comment notifies the requester specifically
    private void notifyOtherParty(Request request, User author) {
        String preview = "New comment on your " + (request.getConstraintKind() != null ? "constraint" : "change request")
                + " (#" + request.getId() + ") from " + author.getFullName() + ".";

        boolean authorIsRequester = author.getId().equals(request.getRequester().getId());
        if (authorIsRequester) {
            notificationService.notifyAllTimetablingTeam("NEW_COMMENT", preview, request);
        } else {
            notificationService.notify(request.getRequester(), "NEW_COMMENT", preview, request, null);
        }
    }

    private void assertCanAccess(Request request, User user) {
        boolean isRequester = request.getRequester().getId().equals(user.getId());
        boolean isTeamOrAdmin = user.getRole() == Role.TIMETABLING_TEAM || user.getRole() == Role.ADMIN;
        if (!isRequester && !isTeamOrAdmin) {
            throw new AccessDeniedException("You don't have access to this request");
        }
    }

    private Request findRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found: " + id));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}
