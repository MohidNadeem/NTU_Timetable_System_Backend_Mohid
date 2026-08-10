package com.ntu.timetabling.model;

/**
 * The two stakeholder roles that can log into the system.
 * Students are intentionally NOT a login role - they interact only via the
 * no-login, public-facing layer (FR10/FR11), per FR13.
 */
public enum Role {
    LECTURER,
    TIMETABLING_TEAM
}
