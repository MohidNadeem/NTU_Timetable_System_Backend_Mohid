package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.NotificationDto;
import com.ntu.timetabling.model.Notification;
import com.ntu.timetabling.model.Request;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.NotificationRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void notify(User recipient, String type, String message, Request relatedRequest, TimetableSession relatedSession) {
        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .relatedRequest(relatedRequest)
                .relatedSession(relatedSession)
                .build());
    }

    // Every Timetabling Team member gets the same notification - it's a shared team queue.
    public void notifyAllTimetablingTeam(String type, String message, Request relatedRequest) {
        for (User member : userRepository.findByRoleOrderByFullNameAsc(Role.TIMETABLING_TEAM)) {
            notify(member, type, message, relatedRequest, null);
        }
    }

    public List<NotificationDto> getForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream().map(NotificationDto::fromEntity).toList();
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    public void markRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));
        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new IllegalArgumentException("This notification does not belong to you");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        List<Notification> unread = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
