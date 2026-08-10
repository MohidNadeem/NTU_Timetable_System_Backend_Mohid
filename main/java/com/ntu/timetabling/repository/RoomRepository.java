package com.ntu.timetabling.repository;

import com.ntu.timetabling.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
