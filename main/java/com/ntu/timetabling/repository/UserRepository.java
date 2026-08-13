package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Role;
import com.ntu.timetabling.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    // using this to list teaching staff for the timetable's teacher filter dropdown
    List<User> findByRoleOrderByFullNameAsc(Role role);
}
