package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.CommentDto;
import com.ntu.timetabling.dto.CreateCommentDto;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.RequestComment;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.RequestCommentRepository;
import com.ntu.timetabling.repository.RequestRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The "2-way communication" thread on a request
 * Out of scope for now: File attachment (Future Work)
 */
@Service
@RequiredArgsConstructor
public class RequestCommentService {

    private final RequestCommentRepository requestCommentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<CommentDto> getComments(Long requestId, String username) {
        Request request = findRequest(requestId);
        User user = findUser(username);
        assertCanAccess(request, user);

        return requestCommentRepository.findByRequestIdOrderByCreatedAtAsc(requestId)
                .stream().map(CommentDto::fromEntity).toList();
    }

    public CommentDto addComment(Long requestId, String username, CreateCommentDto dto) {
        Request request = findRequest(requestId);
        User author = findUser(username);
        assertCanAccess(request, author);

        RequestComment saved = requestCommentRepository.save(RequestComment.builder()
                .request(request)
                .author(author)
                .comment(dto.getComment())
                .build());

        notifyOtherParty(request, author);

        return CommentDto.fromEntity(saved);
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
