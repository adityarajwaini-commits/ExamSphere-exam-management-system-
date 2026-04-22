package com.exam.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class SimpleLogger {
    private static final String LOG_FILE = "exam_system.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SimpleLogger() {
    }

    public static void log(String level, String message) {
        String entry = LocalDateTime.now().format(FORMATTER)
                + " [" + level + "] " + message + System.lineSeparator();
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(entry);
        } catch (IOException ex) {
            // Avoid recursive logging failures
        }
    }
}
