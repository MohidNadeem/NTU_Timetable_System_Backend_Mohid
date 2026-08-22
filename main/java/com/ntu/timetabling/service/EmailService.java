package com.ntu.timetabling.service;

import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.model.EmailLog;
import com.ntu.timetabling.model.EmailStatus;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Real Gmail SMTP sending, gated by app.mail.enabled
 * Every attempt is logged to email_log regardless of whether sending is enabled, so the exact content
 * and intended recipient stay visible/testable even without live credentials configured -
 * disabled sends log as FAILED with a clear reason rather than silently vanishing.
 *
 * Sends HTML (via MimeMessageHelper), not plain text - the htmlBody passed in should already be
 * a full template, typically built with EmailTemplateBuilder.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.mail.enabled}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public void send(String toEmail, String subject, String htmlBody, Course relatedCourse, TimetableSession relatedSession) {
        EmailLog.EmailLogBuilder logEntry = EmailLog.builder()
                .recipientEmail(toEmail)
                .subject(subject)
                .body(htmlBody)
                .relatedCourse(relatedCourse)
                .relatedSession(relatedSession);

        if (!mailEnabled) {
            log.info("[email disabled] would have sent to {} — subject: {}", toEmail, subject);
            emailLogRepository.save(logEntry
                    .status(EmailStatus.FAILED)
                    .errorMessage("Email sending is disabled (set MAIL_ENABLED=true and configure GMAIL_ADDRESS/GMAIL_APP_PASSWORD)")
                    .build());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, "NTU Timetable System");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            mailSender.send(message);

            emailLogRepository.save(logEntry.status(EmailStatus.SENT).build());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            emailLogRepository.save(logEntry
                    .status(EmailStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build());
        }
    }
}
