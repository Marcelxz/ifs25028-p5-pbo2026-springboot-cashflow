package org.delcom.ifs25028springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {

    private static final List<String> KEYS = List.of(
            "DOTENV_A", "DOTENV_B", "DOTENV_C", "DOTENV_D",
            "DOTENV_E", "DOTENV_F", "DOTENV_G", "DOTENV_H", "DOTENV_PROP");

    @TempDir
    Path tempDir;

    @AfterEach
    void bersihkan() {
        KEYS.forEach(System::clearProperty);
    }

    @Test
    void berkasTidakAdaDiabaikan() {
        assertDoesNotThrow(() -> DotEnv.load(tempDir.resolve("tidak-ada.env")));
    }

    @Test
    void memuatBerkasEnv() throws IOException {
        Path env = tempDir.resolve(".env");
        Files.writeString(env, String.join("\n",
                "# komentar",
                "",
                "NOEQUALS",
                "=tanpa_kunci",
                "DOTENV_A=\"dua kutip\"",
                "DOTENV_B='satu kutip'",
                "DOTENV_C=\"setengah",
                "DOTENV_D='setengah",
                "DOTENV_E=biasa",
                "DOTENV_F=x",
                "DOTENV_G=",
                "  DOTENV_H = berspasi  ",
                "DOTENV_PROP=dari_file",
                "PATH=dari_file"));

        System.setProperty("DOTENV_PROP", "dari_property");

        DotEnv.load(env);

        assertEquals("dua kutip", System.getProperty("DOTENV_A"));
        assertEquals("satu kutip", System.getProperty("DOTENV_B"));
        assertEquals("\"setengah", System.getProperty("DOTENV_C"));
        assertEquals("'setengah", System.getProperty("DOTENV_D"));
        assertEquals("biasa", System.getProperty("DOTENV_E"));
        assertEquals("x", System.getProperty("DOTENV_F"));
        assertEquals("", System.getProperty("DOTENV_G"));
        assertEquals("berspasi", System.getProperty("DOTENV_H"));
        // property yang sudah ada tidak ditimpa
        assertEquals("dari_property", System.getProperty("DOTENV_PROP"));
        // environment variable yang sudah ada (PATH) tidak ditimpa
        assertNull(System.getProperty("PATH"));
        assertNull(System.getProperty("NOEQUALS"));
    }
}