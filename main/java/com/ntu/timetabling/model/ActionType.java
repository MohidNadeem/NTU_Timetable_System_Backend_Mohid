package com.ntu.timetabling.model;

// what the Timetabling Team needs to do (if anything) to make the schedule match a request
public enum ActionType {
    NONE,            // schedule already matches - nothing to do
    UPDATE_SESSION,  // an existing session needs its day/time/room (or teacher) changed
    ADD_SESSION,     // a new session needs to be created (change requests, "Additional session")
    CANCEL_SESSION,  // an existing session needs to be cancelled ("Session removal", "Merge sessions/groups")
    MANUAL_REVIEW    // no session to point an automatic action at - needs a human look
}
