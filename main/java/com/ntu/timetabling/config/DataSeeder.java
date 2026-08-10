package com.ntu.timetabling.config;

import com.ntu.timetabling.model.*;
import com.ntu.timetabling.repository.ModuleRepository;
import com.ntu.timetabling.repository.RoomRepository;
import com.ntu.timetabling.repository.TimetableSessionRepository;
import com.ntu.timetabling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Seeds admin-provisioned accounts and a small set of reference/timetable
 * data so the app is usable immediately after a fresh DB setup.
 *
 * There is no self-registration flow in this system (per the project's
 * requirements): accounts are handed out by an admin, and users are only
 * required to change their password on first login.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final RoomRepository roomRepository;
    private final TimetableSessionRepository timetableSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedTimetableSessions();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("lecturer1")) {
            userRepository.save(User.builder()
                    .username("alice.c.l")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .fullName("Dr. Alice Carter")
                    .role(Role.LECTURER)
                    .mustChangePassword(true)
                    .build());
        }

        if (!userRepository.existsByUsername("ttadmin1")) {
            userRepository.save(User.builder()
                    .username("mohid.n.t")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .fullName("Mohid Nadeem")
                    .role(Role.TIMETABLING_TEAM)
                    .mustChangePassword(true)
                    .build());
        }
    }

    private void seedTimetableSessions() {
        if (timetableSessionRepository.count() > 0) {
            return; // already seeded
        }
        if (moduleRepository.count() == 0 || roomRepository.count() == 0) {
            return; // schema.sql module/room seed rows not present yet
        }

        List<ModuleEntity> modules = moduleRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        User lecturer = userRepository.findByUsername("lecturer1").orElse(null);

        if (lecturer == null || modules.isEmpty() || rooms.isEmpty()) {
            return;
        }

        timetableSessionRepository.save(TimetableSession.builder()
                .module(modules.get(0))
                .room(rooms.get(0))
                .lecturer(lecturer)
                .sessionType(SessionType.LECTURE)
                .dayOfWeek(Weekday.MON)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .build());

        timetableSessionRepository.save(TimetableSession.builder()
                .module(modules.get(Math.min(1, modules.size() - 1)))
                .room(rooms.get(Math.min(1, rooms.size() - 1)))
                .lecturer(lecturer)
                .sessionType(SessionType.SEMINAR)
                .dayOfWeek(Weekday.WED)
                .startTime(LocalTime.of(13, 0))
                .endTime(LocalTime.of(14, 0))
                .build());
    }
}
