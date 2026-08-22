package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    // using this to list teaching staff for the timetable's teacher filter dropdown
    List<User> findByRoleOrderByFullNameAsc(Role role);
    // Admin's user-management screen - listing every account, optionally filtered by role
    List<User> findAllByOrderByRoleAscFullNameAsc();
    // course-change email trigger - every active student enrolled in a given course
    List<User> findByRoleAndCourseIdAndAccountStatus(Role role, Long courseId, com.ntu.timetabling.model.AccountStatus status);
    // course-deletion safety check - don't delete a course students are still enrolled in
    boolean existsByCourseId(Long courseId);
}
