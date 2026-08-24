package com.ntu.timetabling.service;

import com.ntu.timetabling.model.AccountStatus;
import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.Room;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The trigger for "email students when their course's timetable changes"
 * called from SessionUpdateService whenever
 *      a session is created,
 *      updated (fully or for specific weeks), or
 *      cancelled (fully or for specific weeks).
 * Every ACTIVE student enrolled in one of the session's tagged courses gets a personalised HTML email.
 * The session's own lecturer gets an in-app notification (not an email - only students are emailed per the current scope).
 */
@Service
@RequiredArgsConstructor
public class CourseChangeNotificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public void notifySessionCreated(TimetableSession session, User actor) {
        String subject = "New session added — " + session.getModule().getCode();
        emailStudentsForSession(session, subject, student -> EmailTemplateBuilder.create()
                .heading("New Session Added")
                .greeting(student.getFullName())
                .intro("A new session has been added to your " + moduleRef(session) + " timetable.")
                .detail("Day & Time", session.getDayOfWeek() + " " + fmt(session.getStartTime()) + "–" + fmt(session.getEndTime()))
                .detail("Room", session.getRoom().getName())
                .detail("Taught by", session.getLecturer().getFullName())
                .closing("If you have any questions, please get in touch with your department.")
                .build());

        String summary = "A new " + session.getSessionType().name().toLowerCase() + " session was added for "
                + session.getModule().getCode() + " (" + session.getDayOfWeek() + " " + session.getStartTime() + ").";
        notificationService.notify(session.getLecturer(), "SESSION_CREATED", summary, null, session);
        activityLogService.log("SESSION_CREATED", summary, actor, session.getLecturer(), session, null);
    }

    // ALL_REMAINING scope - the session's regular weekly pattern has changed, going forward.
    public void notifySessionUpdated(TimetableSession session, User actor) {
        String subject = "Session updated — " + session.getModule().getCode();
        emailStudentsForSession(session, subject, student -> EmailTemplateBuilder.create()
                .heading("Session Time Changed")
                .greeting(student.getFullName())
                .intro("Your " + moduleRef(session) + " session has moved. From now on, it will take place as follows:")
                .detail("Day & Time", session.getDayOfWeek() + " " + fmt(session.getStartTime()) + "–" + fmt(session.getEndTime()))
                .detail("Room", session.getRoom().getName())
                .detail("Taught by", session.getLecturer().getFullName())
                .closing("This applies to every remaining week this block, unless you hear from us again.")
                .build());

        String summary = "Session updated for " + session.getModule().getCode() + " — now "
                + session.getDayOfWeek() + " " + session.getStartTime() + ", " + session.getRoom().getName() + ".";
        notificationService.notify(session.getLecturer(), "SESSION_UPDATED",
                "Your " + session.getModule().getCode() + " session was updated by the Timetabling Team — "
                        + "now " + session.getDayOfWeek() + " " + session.getStartTime() + ", " + session.getRoom().getName() + ".",
                null, session);
        activityLogService.log("SESSION_UPDATED", summary, actor, session.getLecturer(), session, null);
    }

    // SINGLE/MULTIPLE scope - only the given weeks change; the regular pattern is unaffected.
    public void notifySessionUpdatedForWeeks(TimetableSession session, Set<Integer> weeks,
                                              String newDay, LocalTime newStart, LocalTime newEnd, Room newRoom, User actor) {
        String subject = "Session changing for " + weekList(weeks) + " — " + session.getModule().getCode();
        String weeksText = weekList(weeks);
        emailStudentsForSession(session, subject, student -> EmailTemplateBuilder.create()
                .heading("Session Changing for " + weeksText)
                .greeting(student.getFullName())
                .intro("Your " + moduleRef(session) + " session is changing for " + weeksText + " only. "
                        + "Every other week, it stays at its usual time.")
                .detail("New Day & Time", newDay + " " + fmt(newStart) + "–" + fmt(newEnd))
                .detail("Room", (newRoom != null ? newRoom.getName() : session.getRoom().getName()))
                .closing("Outside of " + weeksText + ", nothing changes — you'll see the session at its normal time as usual.")
                .build());

        String summary = "Session updated for " + session.getModule().getCode() + " for " + weeksText + " only.";
        notificationService.notify(session.getLecturer(), "SESSION_UPDATED",
                "Your " + session.getModule().getCode() + " session was updated by the Timetabling Team for "
                        + weeksText + " only.",
                null, session);
        activityLogService.log("SESSION_UPDATED", summary, actor, session.getLecturer(), session, null);
    }

    // ALL_REMAINING (full) cancellation - the session no longer runs at all, going forward.
    public void notifySessionCancelled(TimetableSession session, User actor) {
        String subject = "Session cancelled — " + session.getModule().getCode();
        emailStudentsForSession(session, subject, student -> EmailTemplateBuilder.create()
                .heading("Session Cancelled")
                .greeting(student.getFullName())
                .intro("Your " + moduleRef(session) + " session has been cancelled and will no longer run.")
                .detail("Was scheduled", session.getDayOfWeek() + " " + fmt(session.getStartTime()) + "–" + fmt(session.getEndTime()))
                .detail("Room", session.getRoom().getName())
                .closing("If you have any questions about this, please get in touch with your department.")
                .build());

        String summary = "Session cancelled for " + session.getModule().getCode() + " (" + session.getDayOfWeek() + " " + session.getStartTime() + ").";
        notificationService.notify(session.getLecturer(), "SESSION_CANCELLED",
                "Your " + session.getModule().getCode() + " session (" + session.getDayOfWeek() + " "
                        + session.getStartTime() + ") was cancelled by the Timetabling Team.",
                null, session);
        activityLogService.log("SESSION_CANCELLED", summary, actor, session.getLecturer(), session, null);
    }

    // SINGLE/MULTIPLE (partial) cancellation - the session is skipped for specific weeks only.
    public void notifySessionCancelledForWeeks(TimetableSession session, Set<Integer> weeks, User actor) {
        String weeksText = weekList(weeks);
        String subject = "Session cancelled for " + weeksText + " — " + session.getModule().getCode();
        emailStudentsForSession(session, subject, student -> EmailTemplateBuilder.create()
                .heading("Session Cancelled for " + weeksText)
                .greeting(student.getFullName())
                .intro("Your " + moduleRef(session) + " session will not run in " + weeksText + ".")
                .detail("Normally scheduled", session.getDayOfWeek() + " " + fmt(session.getStartTime()) + "–" + fmt(session.getEndTime()))
                .detail("Room", session.getRoom().getName())
                .closing("The session will resume as normal after " + weeksText + ".")
                .build());

        String summary = "Session cancelled for " + session.getModule().getCode() + " for " + weeksText + " only.";
        notificationService.notify(session.getLecturer(), "SESSION_CANCELLED",
                "Your " + session.getModule().getCode() + " session was cancelled by the Timetabling Team for "
                        + weeksText + " only.",
                null, session);
        activityLogService.log("SESSION_CANCELLED", summary, actor, session.getLecturer(), session, null);
    }

    private void emailStudentsForSession(TimetableSession session, String subject, java.util.function.Function<User, String> bodyBuilder) {
        // if the session's own label names a specific group (e.g. "Lab — Group A"),
        // only students in that same group need emailing
        for (Course course : session.getCourses()) {
            List<User> students = userRepository.findByRoleAndCourseIdAndAccountStatus(
                    Role.STUDENT, course.getId(), AccountStatus.ACTIVE);
            for (User student : students) {
                if (!GroupLabelUtil.isRelevantToStudent(session.getSessionLabel(), student.getGroupLabel())) {
                    continue;
                }
                emailService.send(student.getEmail(), subject, bodyBuilder.apply(student), course, session);
            }
        }
    }

    private String moduleRef(TimetableSession session) {
        String code = EmailTemplateBuilder.escapeHtml(session.getModule().getCode());
        String name = EmailTemplateBuilder.escapeHtml(session.getModule().getName());
        return "<strong>" + code + " — " + name + "</strong>";
    }

    private String fmt(LocalTime t) {
        return t.toString().length() >= 5 ? t.toString().substring(0, 5) : t.toString();
    }

    // "Week 4" for one, "Weeks 3, 4 and 7" for several
    private String weekList(Set<Integer> weeks) {
        List<Integer> sorted = weeks.stream().sorted().toList();
        if (sorted.size() == 1) return "Week " + sorted.get(0);
        String allButLast = sorted.subList(0, sorted.size() - 1).stream().map(String::valueOf).collect(Collectors.joining(", "));
        return "Weeks " + allButLast + " and " + sorted.get(sorted.size() - 1);
    }
}
