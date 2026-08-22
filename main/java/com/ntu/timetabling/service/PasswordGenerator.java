package com.ntu.timetabling.service;

import java.security.SecureRandom;

/**
 * Generates a random default password for Admin-created accounts, which then gets emailed to
 * the new user once (plaintext, only at this one point) alongside their username, per the
 * existing must_change_password flow forcing them to set their own on first login.
 */
public final class PasswordGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
