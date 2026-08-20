package com.ntu.timetabling.service;

import java.util.Map;

/**
 * Week-count per teaching block, mirrored from the frontend's academicCalendar.js
 * Needed server-side for "restrict this new session to specific weeks only"
 */
public final class AcademicBlockConfig {

    private static final Map<Integer, Integer> WEEKS_PER_BLOCK = Map.of(
            1, 10,
            2, 10,
            3, 8,
            4, 1
    );

    private AcademicBlockConfig() {}

    public static int weeksInBlock(int block) {
        return WEEKS_PER_BLOCK.getOrDefault(block, 10);
    }
}
