package com.ntu.timetabling.service;

import com.ntu.timetabling.model.Room;
import com.ntu.timetabling.model.TimetableSession;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.model.Weekday;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

// Blocks Update Session / Add Session from silently creating a room or teacher double-booking.
@Service
@RequiredArgsConstructor
public class ClashCheckService {

    private final TimetableSessionRepository timetableSessionRepository;

    // Throws IllegalArgumentException (mapped to a 400 by GlobalExceptionHandler)
    // if the given day/time/room or day/time/lecturer combination is already taken by another session in the same block
    public void assertNoClash(Long excludeSessionId, int block, Weekday day, LocalTime start, LocalTime end,
                               Room room, User lecturer) {
        List<TimetableSession> overlapping = timetableSessionRepository.findAll().stream()
                .filter(s -> excludeSessionId == null || !s.getId().equals(excludeSessionId))
                .filter(s -> s.getBlock() == block)
                .filter(s -> s.getDayOfWeek() == day)
                .filter(s -> start.isBefore(s.getEndTime()) && s.getStartTime().isBefore(end))
                .toList();

        for (TimetableSession s : overlapping) {
            if (s.getRoom().getId().equals(room.getId())) {
                throw new IllegalArgumentException(
                        "Room clash: " + room.getName() + " is already booked by " + s.getModule().getCode()
                                + " (" + s.getLecturer().getFullName() + ") " + day + " "
                                + s.getStartTime() + "-" + s.getEndTime() + " in this block");
            }
            if (s.getLecturer().getId().equals(lecturer.getId())) {
                throw new IllegalArgumentException(
                        "Teacher clash: " + lecturer.getFullName() + " is already teaching " + s.getModule().getCode()
                                + " " + day + " " + s.getStartTime() + "-" + s.getEndTime() + " in this block");
            }
        }
    }
}
