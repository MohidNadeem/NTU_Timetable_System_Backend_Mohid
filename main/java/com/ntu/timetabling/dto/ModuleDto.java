package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.ModuleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ModuleDto {
    private Long id;
    private String code;
    private String name;

    public static ModuleDto fromEntity(ModuleEntity m) {
        return ModuleDto.builder().id(m.getId()).code(m.getCode()).name(m.getName()).build();
    }
}
