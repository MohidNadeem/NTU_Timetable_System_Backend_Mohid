package com.ntu.timetabling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * "Cancel Session" (Session Removal category, and the removal step of a Merge)
 */
@Getter
@Setter
public class CancelSessionDto {

    @NotBlank
    private String scope; // ALL_REMAINING | SINGLE | MULTIPLE

    private Set<Integer> weeks; // required for SINGLE/MULTIPLE

    // which request this cancellation fulfils
    private Long relatedRequestId;

    private String reason;
}
