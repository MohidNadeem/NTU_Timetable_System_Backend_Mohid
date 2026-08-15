package com.ntu.timetabling.dto;

import com.ntu.timetabling.model.ActionType;
import com.ntu.timetabling.model.ChangeCategory;
import com.ntu.timetabling.model.ConstraintKind;
import com.ntu.timetabling.model.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * The shared "effect calculator" result
 * used for View Effect (any status, a pre-decision preview),
 * Violations (ACCEPTED constraints only), and
 * Changes in Queue (ACCEPTED change requests only).
 */
@Getter
@Builder
@AllArgsConstructor
public class EffectResultDto {
    private Long requestId;
    private RequestType requestType;
    private ConstraintKind constraintKind; // null for CHANGE
    private ChangeCategory changeCategory; // null for CONSTRAINT
    private String requesterName;
    private String departmentCode;
    private String primaryModuleCode; // null for PERSONAL constraints
    private String primaryModuleName;
    private Integer block;

    // true = schedule already matches what was requested - nothing to do
    private boolean satisfied;
    private ActionType actionType;

    private List<EffectItemDto> items;
}
