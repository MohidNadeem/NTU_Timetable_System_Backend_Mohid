package com.ntu.timetabling.model;

public enum RequestType {
    CONSTRAINT, // Increment 1: FR1/FR2 - submitted ahead of the annual timetable
    CHANGE      // Increment 2: FR3-FR9 - submitted against an existing live session
}
