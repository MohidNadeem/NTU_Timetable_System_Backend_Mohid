package com.ntu.timetabling.service;

import com.ntu.timetabling.dto.ChangePasswordRequest;
import com.ntu.timetabling.dto.LoginRequest;
import com.ntu.timetabling.dto.LoginResponse;
import com.ntu.timetabling.model.AccountStatus;
import com.ntu.timetabling.model.User;
import com.ntu.timetabling.repository.UserRepository;
import com.ntu.timetabling.security.JwtService;
import com.ntu.timetabling.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Leaver (staff) / Alumni (students) accounts are blocked from logging in
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException(
                    "This account is no longer active (" + user.getAccountStatus().name().toLowerCase()
                            + "). Please contact your administrator.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails, Map.of(
                "role", user.getRole().name(),
                "fullName", user.getFullName()
        ));

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .mustChangePassword(user.isMustChangePassword())
                .courseId(user.getCourse() != null ? user.getCourse().getId() : null)
                .build();
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }
}
