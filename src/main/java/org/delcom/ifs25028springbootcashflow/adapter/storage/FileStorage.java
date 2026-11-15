package org.delcom.ifs25028springbootcashflow.adapter.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;

/** Menyimpan file upload (foto profil & bukti transaksi) ke UPLOAD_DIR. */
public class FileStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membuat folder upload", e);
        }
    }

    public Path getRoot() {
        return root;
    }

    /** Mengembalikan nama file relatif (UUID + ekstensi), diakses lewat /uploads/{nama}. */
    public String store(String originalName, byte[] data) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        String extension = extensionOf(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Format gambar harus png, jpg, jpeg, gif, atau webp");
        }
        String fileName = UUID.randomUUID() + "." + extension;
        try {
            Files.write(root.resolve(fileName), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menyimpan file", e);
        }
        return fileName;
    }

    public void deleteQuietly(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // file yatim piatu tidak fatal
        }
    }

    private String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}