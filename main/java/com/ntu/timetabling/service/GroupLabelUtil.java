package com.ntu.timetabling.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GroupLabelUtil {

    private static final Pattern GROUP_PATTERN = Pattern.compile("Group [A-Za-z0-9]+");

    private GroupLabelUtil() {}

    // pulls "Group A" (or similar) out of a session label like "Lab — Group A"
    public static String extractGroupLabel(String sessionLabel) {
        if (sessionLabel == null) return null;
        Matcher m = GROUP_PATTERN.matcher(sessionLabel);
        return m.find() ? m.group() : null;
    }

    // true if a student should see/be notified about a session with the given label
    public static boolean isRelevantToStudent(String sessionLabel, String studentGroupLabel) {
        String sessionGroup = extractGroupLabel(sessionLabel);
        if (sessionGroup == null) return true;
        if (studentGroupLabel == null) return true;
        return sessionGroup.equals(studentGroupLabel);
    }
}
