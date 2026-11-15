package org.delcom.ifs25028springbootcashflow.adapter.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.delcom.ifs25028springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageTest {

    private final byte[] data = {1, 2, 3};

    @TempDir
    Path tempDir;

    @Test
    void konstruktorMembuatFolder() {
        FileStorage storage = new FileStorage(tempDir.resolve("a/b").toString());
        assertTrue(Files.isDirectory(storage.getRoot()));
    }

    @Test
    void konstruktorGagalBilaFolderTidakBisaDibuat() throws IOException {
        Path berkas = Files.createFile(tempDir.resolve("berkas"));
        String lokasi = berkas.resolve("sub").toString();
        assertThrows(UncheckedIOException.class, () -> new FileStorage(lokasi));
    }

    @Test
    void simpanBerhasil() throws Exception {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        String nama = storage.store("foto.PNG", data);

        assertTrue(nama.endsWith(".png"));
        assertArrayEquals(data, Files.readAllBytes(storage.getRoot().resolve(nama)));
    }

    @Test
    void simpanDataKosongDitolak() {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        assertThrows(ValidationException.class, () -> storage.store("a.png", null));
        assertThrows(ValidationException.class, () -> storage.store("a.png", new byte[0]));
    }

    @Test
    void ekstensiTidakDiizinkanDitolak() {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        assertThrows(ValidationException.class, () -> storage.store("dokumen.pdf", data));
        assertThrows(ValidationException.class, () -> storage.store("tanpaekstensi", data));
        assertThrows(ValidationException.class, () -> storage.store(null, data));
    }

    @Test
    void simpanGagalBilaFolderHilang() throws IOException {
        Path root = tempDir.resolve("uploads");
        FileStorage storage = new FileStorage(root.toString());
        Files.delete(root);
        assertThrows(UncheckedIOException.class, () -> storage.store("a.png", data));
    }

    @Test
    void hapusFile() throws Exception {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        String nama = storage.store("a.png", data);

        storage.deleteQuietly(nama);
        assertFalse(Files.exists(storage.getRoot().resolve(nama)));

        // file sudah tidak ada: tidak boleh error
        assertDoesNotThrow(() -> storage.deleteQuietly(nama));
    }

    @Test
    void hapusNamaKosongDiabaikan() {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        assertDoesNotThrow(() -> storage.deleteQuietly(null));
        assertDoesNotThrow(() -> storage.deleteQuietly(""));
        assertDoesNotThrow(() -> storage.deleteQuietly("   "));
    }

    @Test
    void hapusDiLuarFolderUploadDitolak() throws IOException {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        Path luar = Files.createFile(tempDir.resolve("luar.png"));

        storage.deleteQuietly("../luar.png");

        assertTrue(Files.exists(luar));
    }

    @Test
    void hapusFolderTidakKosongDiabaikan() throws IOException {
        FileStorage storage = new FileStorage(tempDir.resolve("uploads").toString());
        Path folder = Files.createDirectory(storage.getRoot().resolve("folder"));
        Files.createFile(folder.resolve("isi.txt"));

        assertDoesNotThrow(() -> storage.deleteQuietly("folder"));
        assertTrue(Files.exists(folder));
    }
}