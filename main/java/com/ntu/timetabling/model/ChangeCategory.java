package com.ntu.timetabling.model;

public enum ChangeCategory {
    SESSION_TIME,
    CLASHES,
    ROOM_TYPE,
    ADDITIONAL_SESSION,
    ROOM_BOOKING,
    STUDENT_ALLOCATION,
    STAFF_CHANGE,        // must be approved
    SESSION_DATE,        // must be approved
    SESSION_REMOVAL,     // must be approved
    MERGE_SESSIONS_GROUPS, // must be approved
    OTHER
}
