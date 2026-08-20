package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.EffectItemDto;
import com.ntu.timetabling.dto.EffectResultDto;
import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.SessionOverrideRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The shared "effect calculator"
 * "if this request is/were accepted, does the current schedule already match it?"
 * Used by View Effect,
 * Violations (ACCEPTED constraints only), and
 * Changes in Queue (ACCEPTED change requests only).
 */
@Service
@RequiredArgsConstructor
public class EffectService {

    private final TimetableSessionRepository timetableSessionRepository;
    private final SessionOverrideRepository sessionOverrideRepository;

    public EffectResultDto computeEffect(Request r) {
        if (r.getType() == RequestType.CONSTRAINT) {
            return r.getConstraintKind() == ConstraintKind.MODULE
                    ? computeModuleConstraintEffect(r)
                    : computePersonalConstraintEffect(r);
        }
        return computeChangeRequestEffect(r);
    }

    // ---- MODULE constraint -------

    private EffectResultDto computeModuleConstraintEffect(Request r) {
        ModuleEntity module = r.getPrimaryModule();
        SessionType inferredType = inferSessionType(r.getLearningActivity());

        List<TimetableSession> candidates = timetableSessionRepository.findByModuleId(module.getId()).stream()
                .filter(s -> inferredType == null || s.getSessionType() == inferredType)
                .toList();

        Set<String> allowedRoomNames = r.getAllowedRooms().stream().map(Room::getName).collect(Collectors.toSet());
        List<RequestGroup> groups = r.getGroups();
        List<EffectItemDto> items = new ArrayList<>();

        if (!groups.isEmpty()) {
            for (RequestGroup group : groups) {
                TimetableSession matched;
                if (group.getPreferredLecturer() != null) {
                    matched = candidates.stream()
                            .filter(s -> s.getLecturer().getId().equals(group.getPreferredLecturer().getId()))
                            .findFirst().orElse(null);
                } else if (candidates.size() == 1) {
                    matched = candidates.get(0);
                } else {
                    matched = null;
                }

                if (matched != null) {
                    CompareResult cmp = compareAgainstTarget(matched, r, allowedRoomNames);
                    if (!cmp.satisfied()) {
                        items.add(buildItem(matched, module, r, allowedRoomNames, cmp.unmatchedWeeks())
                                .toBuilder().actionType(ActionType.UPDATE_SESSION)
                                .groupLabel(group.getGroupLabel())
                                .preferredLecturerId(group.getPreferredLecturer() != null ? group.getPreferredLecturer().getId() : null)
                                .preferredLecturerName(group.getPreferredLecturer() != null ? group.getPreferredLecturer().getFullName() : null)
                                .build());
                    }
                    // satisfied groups contribute no item - nothing to do for that group
                    continue;
                }

                // no matching session for this group
                // if we have a named teacher (or the module simply has no session of this type at all yet)
                // that's a "create it" case then
                boolean genuinelyAmbiguous = group.getPreferredLecturer() == null && candidates.size() > 1;
                items.add(unmatchedItem(module, r, inferredType, allowedRoomNames)
                        .toBuilder()
                        .actionType(genuinelyAmbiguous ? ActionType.MANUAL_REVIEW : ActionType.ADD_SESSION)
                        .groupLabel(group.getGroupLabel())
                        .preferredLecturerId(group.getPreferredLecturer() != null ? group.getPreferredLecturer().getId() : null)
                        .preferredLecturerName(group.getPreferredLecturer() != null ? group.getPreferredLecturer().getFullName() : null)
                        .build());
            }
        } else if (candidates.size() > 1) {
            // no groups given, but more than one session of this type exists - genuinely can't
            items.add(unmatchedItem(module, r, inferredType, allowedRoomNames)
                    .toBuilder().actionType(ActionType.MANUAL_REVIEW).build());
        } else if (candidates.isEmpty()) {
            items.add(unmatchedItem(module, r, inferredType, allowedRoomNames)
                    .toBuilder().actionType(ActionType.ADD_SESSION).build());
        } else {
            TimetableSession matched = candidates.get(0);
            CompareResult cmp = compareAgainstTarget(matched, r, allowedRoomNames);
            if (!cmp.satisfied()) {
                items.add(buildItem(matched, module, r, allowedRoomNames, cmp.unmatchedWeeks())
                        .toBuilder().actionType(ActionType.UPDATE_SESSION).build());
            }
        }

        return summarise(r, module.getCode(), module.getName(), items);
    }

    // ---- PERSONAL constraint --------

    private EffectResultDto computePersonalConstraintEffect(Request r) {
        Set<Weekday> unavailableDays = r.getUnavailableDays();

        if (unavailableDays == null || unavailableDays.isEmpty()) {
            return baseBuilder(r).satisfied(true).actionType(ActionType.NONE).items(List.of()).build();
        }

        LocalTime from = r.getUnavailableFromTime();
        LocalTime to = r.getUnavailableToTime();
        LocalTime effFrom = from != null ? from : LocalTime.MIN;
        LocalTime effTo = to != null ? to : LocalTime.MAX;

        List<EffectItemDto> items = new ArrayList<>();
        for (TimetableSession s : timetableSessionRepository.findByLecturerId(r.getRequester().getId())) {
            if (!unavailableDays.contains(s.getDayOfWeek())) continue;
            boolean overlaps = s.getStartTime().isBefore(effTo) && effFrom.isBefore(s.getEndTime());
            if (!overlaps) continue;

            items.add(EffectItemDto.builder()
                    .sessionId(s.getId())
                    .moduleCode(s.getModule().getCode())
                    .moduleName(s.getModule().getName())
                    .sessionType(s.getSessionType().name())
                    .actionType(ActionType.UPDATE_SESSION)
                    .currentDayOfWeek(s.getDayOfWeek().name())
                    .currentStartTime(s.getStartTime())
                    .currentEndTime(s.getEndTime())
                    .currentRoomName(s.getRoom().getName())
                    .requestedDayOfWeek(s.getDayOfWeek().name())
                    .requestedStartTime(from)
                    .requestedEndTime(to)
                    .build());
        }

        return baseBuilder(r).satisfied(items.isEmpty())
                .actionType(items.isEmpty() ? ActionType.NONE : ActionType.UPDATE_SESSION)
                .items(items).build();
    }

    // ---- CHANGE request ---------

    private EffectResultDto computeChangeRequestEffect(Request r) {
        ModuleEntity module = r.getPrimaryModule();
        ChangeCategory category = r.getChangeCategory();

        if (category == ChangeCategory.ADDITIONAL_SESSION) {
            boolean alreadyAdded = timetableSessionRepository.existsByRelatedRequestId(r.getId());
            Set<String> allowedRoomNames = r.getAllowedRooms().stream().map(Room::getName).collect(Collectors.toSet());

            List<EffectItemDto> items = alreadyAdded ? List.of() : List.of(EffectItemDto.builder()
                    .moduleCode(module.getCode())
                    .moduleName(module.getName())
                    .actionType(ActionType.ADD_SESSION)
                    .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                    .requestedStartTime(r.getStartTime())
                    .requestedEndTime(r.getEndTime())
                    .requestedRoomName(allowedRoomNames.isEmpty() ? null : String.join(" / ", allowedRoomNames))
                    .build());

            return summarise(r, module.getCode(), module.getName(), items);
        }

        if (category == ChangeCategory.CLASHES) {
            return computeClashesEffect(r, module);
        }

        if (category == ChangeCategory.STAFF_CHANGE) {
            return computeStaffChangeEffect(r, module);
        }

        if (category == ChangeCategory.SESSION_REMOVAL) {
            return computeSessionRemovalEffect(r, module);
        }

        if (category == ChangeCategory.MERGE_SESSIONS_GROUPS) {
            return computeMergeEffect(r, module);
        }

        // default "modify an existing session"
        TimetableSession baseSession = r.getSession();
        if (baseSession == null) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        Set<String> allowedRoomNames = r.getAllowedRooms().stream().map(Room::getName).collect(Collectors.toSet());
        boolean dayGiven = r.getDayOfWeek() != null;
        boolean timeGiven = r.getStartTime() != null;
        boolean roomGiven = !allowedRoomNames.isEmpty();

        if (!dayGiven && !timeGiven && !roomGiven) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        CompareResult cmp = compareAgainstTarget(baseSession, r, allowedRoomNames);
        List<EffectItemDto> items = cmp.satisfied() ? List.of() : List.of(
                buildItem(baseSession, module, r, allowedRoomNames, cmp.unmatchedWeeks())
                        .toBuilder().actionType(ActionType.UPDATE_SESSION).build());

        return summarise(r, module.getCode(), module.getName(), items);
    }

    // Clashes: satisfied once the two sessions no longer overlap in day/time.
    private EffectResultDto computeClashesEffect(Request r, ModuleEntity module) {
        TimetableSession mine = r.getSession();
        TimetableSession clashing = r.getClashingSession();
        if (mine == null || clashing == null) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        boolean stillClashes = mine.getDayOfWeek() == clashing.getDayOfWeek()
                && mine.getStartTime().isBefore(clashing.getEndTime())
                && clashing.getStartTime().isBefore(mine.getEndTime());

        if (!stillClashes) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        EffectItemDto item = EffectItemDto.builder()
                .sessionId(mine.getId())
                .moduleCode(module.getCode())
                .moduleName(module.getName())
                .sessionType(mine.getSessionType().name())
                .actionType(ActionType.UPDATE_SESSION)
                .currentDayOfWeek(mine.getDayOfWeek().name())
                .currentStartTime(mine.getStartTime())
                .currentEndTime(mine.getEndTime())
                .currentRoomName(mine.getRoom().getName())
                // "requested" here means "what it currently clashes with", not a preference -
                .requestedDayOfWeek(clashing.getDayOfWeek().name())
                .requestedStartTime(clashing.getStartTime())
                .requestedEndTime(clashing.getEndTime())
                .requestedRoomName(clashing.getModule().getCode() + " · " + clashing.getRoom().getName())
                .build();

        return summarise(r, module.getCode(), module.getName(), List.of(item));
    }

    // Staff change: satisfied once the session's actual teacher matches who was asked for.
    private EffectResultDto computeStaffChangeEffect(Request r, ModuleEntity module) {
        TimetableSession session = r.getSession();
        User preferred = r.getPreferredNewLecturer();
        if (session == null || preferred == null) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        if (session.getLecturer().getId().equals(preferred.getId())) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        EffectItemDto item = EffectItemDto.builder()
                .sessionId(session.getId())
                .moduleCode(module.getCode())
                .moduleName(module.getName())
                .sessionType(session.getSessionType().name())
                .actionType(ActionType.UPDATE_SESSION)
                .currentDayOfWeek(session.getDayOfWeek().name())
                .currentStartTime(session.getStartTime())
                .currentEndTime(session.getEndTime())
                .currentRoomName(session.getRoom().getName() + " (" + session.getLecturer().getFullName() + ")")
                .requestedRoomName("Requested teacher: " + preferred.getFullName())
                .build();

        return summarise(r, module.getCode(), module.getName(), List.of(item));
    }

    // Session removal: satisfied once the session (fully, or for every requested week) is cancelled.
    private EffectResultDto computeSessionRemovalEffect(Request r, ModuleEntity module) {
        TimetableSession session = r.getSession();
        if (session == null) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        boolean satisfied;
        List<Integer> stillActiveWeeks = List.of();
        if (r.getWeekMode() == null || r.getWeekMode() == WeekMode.ALL_REMAINING) {
            satisfied = session.isFullyCancelled();
        } else {
            stillActiveWeeks = r.getWeeks().stream().filter(session::isActiveInWeek).sorted().toList();
            satisfied = stillActiveWeeks.isEmpty();
        }

        if (satisfied) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        EffectItemDto item = EffectItemDto.builder()
                .sessionId(session.getId())
                .moduleCode(module.getCode())
                .moduleName(module.getName())
                .sessionType(session.getSessionType().name())
                .actionType(ActionType.CANCEL_SESSION)
                .currentDayOfWeek(session.getDayOfWeek().name())
                .currentStartTime(session.getStartTime())
                .currentEndTime(session.getEndTime())
                .currentRoomName(session.getRoom().getName())
                .unmatchedWeeks(stillActiveWeeks.isEmpty() ? null : stillActiveWeeks)
                .build();

        return summarise(r, module.getCode(), module.getName(), List.of(item));
    }

    // Merge sessions/groups: per your instruction, TT first cancels every constituent session,
    // then creates one new session to replace them (Add Session, tracked via relatedRequestId
    // exactly like Additional Session already is).
    // Merge sessions/groups: always exactly one week (enforced at submission) - per your
    // instruction, cancel the constituent sessions for THAT WEEK ONLY, then create one
    // replacement session restricted to that same single week (not a permanent recurring one).
    private EffectResultDto computeMergeEffect(Request r, ModuleEntity module) {
        List<EffectItemDto> items = new ArrayList<>();
        Integer targetWeek = r.getWeeks().stream().findFirst().orElse(null);
        if (targetWeek == null) {
            return summarise(r, module.getCode(), module.getName(), List.of());
        }

        for (TimetableSession s : r.getMergeSessions()) {
            if (!s.isActiveInWeek(targetWeek)) continue; // already cancelled for this specific week
            items.add(EffectItemDto.builder()
                    .sessionId(s.getId())
                    .moduleCode(s.getModule().getCode())
                    .moduleName(s.getModule().getName())
                    .sessionType(s.getSessionType().name())
                    .actionType(ActionType.CANCEL_SESSION)
                    .currentDayOfWeek(s.getDayOfWeek().name())
                    .currentStartTime(s.getStartTime())
                    .currentEndTime(s.getEndTime())
                    .currentRoomName(s.getRoom().getName())
                    .unmatchedWeeks(List.of(targetWeek))
                    .build());
        }

        if (items.isEmpty()) {
            // both constituent sessions are cancelled for the target week - now the one-week
            // merged replacement needs creating
            boolean alreadyAdded = timetableSessionRepository.existsByRelatedRequestId(r.getId());
            if (!alreadyAdded) {
                Set<String> allowedRoomNames = r.getAllowedRooms().stream().map(Room::getName).collect(Collectors.toSet());
                items.add(EffectItemDto.builder()
                        .moduleCode(module.getCode())
                        .moduleName(module.getName())
                        .actionType(ActionType.ADD_SESSION)
                        .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                        .requestedStartTime(r.getStartTime())
                        .requestedEndTime(r.getEndTime())
                        .requestedRoomName(allowedRoomNames.isEmpty() ? null : String.join(" / ", allowedRoomNames))
                        .unmatchedWeeks(List.of(targetWeek))
                        .build());
            }
        }

        return summarise(r, module.getCode(), module.getName(), items);
    }

    // ---- shared comparison logic (module constraints + change requests) --------

    private record CompareResult(boolean satisfied, List<Integer> unmatchedWeeks) {}

    private CompareResult compareAgainstTarget(TimetableSession candidate, Request r, Set<String> allowedRoomNames) {
        boolean dayGiven = r.getDayOfWeek() != null;
        boolean timeGiven = r.getStartTime() != null;
        boolean roomGiven = !allowedRoomNames.isEmpty();

        if (r.getWeekMode() == null || r.getWeekMode() == WeekMode.ALL_REMAINING) {
            boolean dayMismatch = dayGiven && !r.getDayOfWeek().equals(candidate.getDayOfWeek());
            boolean timeMismatch = timeGiven && !r.getStartTime().equals(candidate.getStartTime());
            boolean roomMismatch = roomGiven && !allowedRoomNames.contains(candidate.getRoom().getName());
            return new CompareResult(!dayMismatch && !timeMismatch && !roomMismatch, List.of());
        }

        List<SessionOverride> overrides = sessionOverrideRepository.findBySessionId(candidate.getId());
        List<Integer> unmatchedWeeks = new ArrayList<>();
        for (Integer week : r.getWeeks()) {
            boolean weekSatisfied = overrides.stream().anyMatch(o ->
                    o.getWeeks().contains(week)
                            && (!dayGiven || r.getDayOfWeek().equals(o.getNewDayOfWeek()))
                            && (!timeGiven || r.getStartTime().equals(o.getNewStartTime()))
                            && (!roomGiven || allowedRoomNames.contains(
                                    o.getNewRoom() != null ? o.getNewRoom().getName() : candidate.getRoom().getName()))
            );
            if (!weekSatisfied) unmatchedWeeks.add(week);
        }
        return new CompareResult(unmatchedWeeks.isEmpty(), unmatchedWeeks);
    }

    private EffectItemDto buildItem(TimetableSession candidate, ModuleEntity module, Request r,
                                     Set<String> allowedRoomNames, List<Integer> unmatchedWeeks) {
        return EffectItemDto.builder()
                .sessionId(candidate.getId())
                .moduleCode(module.getCode())
                .moduleName(module.getName())
                .sessionType(candidate.getSessionType().name())
                .currentDayOfWeek(candidate.getDayOfWeek().name())
                .currentStartTime(candidate.getStartTime())
                .currentEndTime(candidate.getEndTime())
                .currentRoomName(candidate.getRoom().getName())
                .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                .requestedStartTime(r.getStartTime())
                .requestedEndTime(r.getEndTime())
                .requestedRoomName(allowedRoomNames.isEmpty() ? null : String.join(" / ", allowedRoomNames))
                .unmatchedWeeks(unmatchedWeeks.isEmpty() ? null : unmatchedWeeks)
                .build();
    }

    private EffectItemDto unmatchedItem(ModuleEntity module, Request r, SessionType inferredType, Set<String> allowedRoomNames) {
        return EffectItemDto.builder()
                .moduleCode(module.getCode())
                .moduleName(module.getName())
                .sessionType(inferredType != null ? inferredType.name() : null)
                .requestedDayOfWeek(r.getDayOfWeek() != null ? r.getDayOfWeek().name() : null)
                .requestedStartTime(r.getStartTime())
                .requestedRoomName(allowedRoomNames.isEmpty() ? null : String.join(" / ", allowedRoomNames))
                .build();
    }

    private EffectResultDto.EffectResultDtoBuilder baseBuilder(Request r) {
        return EffectResultDto.builder()
                .requestId(r.getId())
                .requestType(r.getType())
                .constraintKind(r.getConstraintKind())
                .changeCategory(r.getChangeCategory())
                .requesterName(r.getRequester().getFullName())
                .departmentCode(r.getDepartment() != null ? r.getDepartment().getCode() : null);
    }

    // building the overall (request-level) summary
    private EffectResultDto summarise(Request r, String moduleCode, String moduleName, List<EffectItemDto> items) {
        boolean satisfied = items.isEmpty();
        ActionType overall;
        if (satisfied) {
            overall = ActionType.NONE;
        } else if (items.stream().anyMatch(i -> i.getActionType() == ActionType.MANUAL_REVIEW)) {
            overall = ActionType.MANUAL_REVIEW;
        } else if (items.stream().anyMatch(i -> i.getActionType() == ActionType.ADD_SESSION)) {
            overall = ActionType.ADD_SESSION;
        } else {
            overall = ActionType.UPDATE_SESSION;
        }

        return baseBuilder(r).primaryModuleCode(moduleCode).primaryModuleName(moduleName)
                .block(r.getBlock()).satisfied(satisfied).actionType(overall).items(items).build();
    }

    private SessionType inferSessionType(String learningActivity) {
        if (learningActivity == null) return null;
        String a = learningActivity.toLowerCase(Locale.ROOT);
        if (a.contains("lab") || a.contains("practical")) return SessionType.LAB;
        if (a.contains("seminar")) return SessionType.SEMINAR;
        if (a.contains("tutorial")) return SessionType.TUTORIAL;
        if (a.contains("surgery")) return SessionType.SURGERY;
        if (a.contains("lecture")) return SessionType.LECTURE;
        if (a.contains("workshop")) return SessionType.WORKSHOP;
        if (a.contains("assessment")) return SessionType.ASSESSMENT;
        if (a.contains("drop-in") || a.contains("drop in")) return SessionType.DROP_IN;
        return null;
    }
}
