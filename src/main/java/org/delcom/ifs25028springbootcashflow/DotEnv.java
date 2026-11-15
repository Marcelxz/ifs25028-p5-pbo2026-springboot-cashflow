package org.delcom.ifs25028springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Memuat file .env ke System properties sebelum Spring Boot start. */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() throws IOException {
        load(Path.of(".env"));
    }

    static void load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        for (String raw : Files.readAllLines(file)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = unquote(line.substring(idx + 1).trim());
            // environment server/cloud tetap diprioritaskan
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

    private static String unquote(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}