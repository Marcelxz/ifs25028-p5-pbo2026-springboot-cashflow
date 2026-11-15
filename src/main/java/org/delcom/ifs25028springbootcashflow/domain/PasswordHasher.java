package org.delcom.ifs25028springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String raw) {
        return digest(raw, "SHA-256");
    }

    public static boolean matches(String raw, String hash) {
        return hash(raw).equals(hash);
    }

    // dipisah agar jalur gagal (algoritma tidak ada) bisa diuji
    static String digest(String raw, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " tidak tersedia", e);
        }
    }
}