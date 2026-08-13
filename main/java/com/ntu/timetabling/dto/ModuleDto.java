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
    // which teaching block this module runs in
    // null if the module has no sessions yet. Used to scope the constraint form's week picker.
    private Integer block;

    public static ModuleDto fromEntity(ModuleEntity m) {
        return fromEntity(m, null);
    }

    public static ModuleDto fromEntity(ModuleEntity m, Integer block) {
        return ModuleDto.builder().id(m.getId()).code(m.getCode()).name(m.getName()).block(block).build();
    }
}
