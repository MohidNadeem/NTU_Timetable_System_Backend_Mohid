package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.ModuleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ModuleAdminDto {
    private Long id;
    private String code;
    private String name;
    private List<String> courseCodes;

    public static ModuleAdminDto fromEntity(ModuleEntity m) {
        return ModuleAdminDto.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .courseCodes(m.getCourses().stream().map(c -> c.getCode()).sorted().toList())
                .build();
    }
}
