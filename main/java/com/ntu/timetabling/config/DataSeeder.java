package com.ntu.timetabling.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Deliberately keeping it empty now.
 * The demo data is now seeded entirely by database/schema.sql
 */
@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // no-operation for now
    }
}
