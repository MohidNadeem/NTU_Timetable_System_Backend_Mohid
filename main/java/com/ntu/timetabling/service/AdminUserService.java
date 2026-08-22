package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.CreateUserDto;
import com.ntu.timetabling.dto.UpdateUserDto;
import com.ntu.timetabling.dto.UserAdminDto;
import com.ntu.timetabling.model.AccountStatus;
import com.ntu.timetabling.model.Course;
import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.CourseRepository;
import com.ntu.timetabling.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAllByOrderByRoleAscFullNameAsc()
                .stream().map(UserAdminDto::fromEntity).toList();
    }

    // Creates a new account with a random default password, emailed to the new user once
    public UserAdminDto createUser(CreateUserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already in use: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        Role role = Role.valueOf(dto.getRole());
        Course course = null;
        if (role == Role.STUDENT) {
            if (dto.getCourseId() == null) {
                throw new IllegalArgumentException("A course is required for student accounts");
            }
            course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found: " + dto.getCourseId()));
        }

        String plaintextPassword = PasswordGenerator.generate();

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .course(course)
                .passwordHash(passwordEncoder.encode(plaintextPassword))
                .mustChangePassword(true)
                .build();

        User saved = userRepository.save(user);

        String subject = "Your NTU Timetabling account";
        String roleLabel = switch (role) {
            case ADMIN -> "an Admin";
            case LECTURER -> "a Lecturer";
            case TIMETABLING_TEAM -> "a Timetabling Team member";
            case STUDENT -> "a Student";
        };
        String html = EmailTemplateBuilder.create()
                .heading("Your NTU Timetabling Account")
                .greeting(saved.getFullName())
                .intro("An account has been created for you as " + roleLabel + " on the NTU Timetabling Requests Management system.")
                .detail("Username", saved.getUsername())
                .detail("Email", saved.getEmail())
                .detail("Temporary Password", plaintextPassword)
                .closing("Please sign in using these details. You'll be asked to set your own password the first time you log in.")
                .build();
        emailService.send(saved.getEmail(), subject, html, course, null);

        return UserAdminDto.fromEntity(saved);
    }

    public UserAdminDto updateUser(Long userId, UpdateUserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setAccountStatus(AccountStatus.valueOf(dto.getAccountStatus()));

        if (user.getRole() == Role.STUDENT) {
            if (dto.getCourseId() != null) {
                Course course = courseRepository.findById(dto.getCourseId())
                        .orElseThrow(() -> new EntityNotFoundException("Course not found: " + dto.getCourseId()));
                user.setCourse(course);
            } else {
                user.setCourse(null);
            }
        }

        return UserAdminDto.fromEntity(userRepository.save(user));
    }
}
