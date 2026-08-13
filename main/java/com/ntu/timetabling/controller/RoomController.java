package com.ntu.timetabling.controller;

import com.ntu.timetabling.dto.RoomDto;
import com.ntu.timetabling.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// read-only lookup, available to any authenticated user
// populates the timetable's room filter dropdown
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;

    @GetMapping
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(RoomDto::fromEntity)
                .toList();
    }
}
