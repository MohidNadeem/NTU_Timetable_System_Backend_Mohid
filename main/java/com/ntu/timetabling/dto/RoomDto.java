package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String name;
    private String building;

    public static RoomDto fromEntity(Room r) {
        return RoomDto.builder().id(r.getId()).name(r.getName()).building(r.getBuilding()).build();
    }
}
