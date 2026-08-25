package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.CommentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentAttachmentRepository extends JpaRepository<CommentAttachment, Long> {
    Optional<CommentAttachment> findByCommentId(Long commentId);
}
