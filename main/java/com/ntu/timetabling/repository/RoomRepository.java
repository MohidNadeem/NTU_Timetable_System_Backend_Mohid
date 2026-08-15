package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    // used to resolve the "ONLINE" room type
    Optional<Room> findByName(String name);
}
