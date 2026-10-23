package com.cloudBasedStorageService.StorageContainer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tmp;

    private MockMultipartFile upload(String name, String content) {
        return new MockMultipartFile("file", name, "text/plain",
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void storesUnderTheUserDirectoryAndReturnsARelativeKey() throws IOException {

        FileStorageService storage = new FileStorageService(tmp.toString());

        String key = storage.store(upload("notes.txt", "hello"), 7);

        assertTrue(key.startsWith("7/"), key);
        assertTrue(key.endsWith(".txt"), key);

        Path stored = storage.resolve(key);

        assertEquals("hello", Files.readString(stored));
        assertTrue(stored.startsWith(tmp), "blob must live inside the upload root");
        assertFalse(Path.of(key).isAbsolute(), "persisted key must stay relative");
    }

    @Test
    void rejectsAStoredKeyThatWalksOutsideTheUploadRoot() {

        FileStorageService storage = new FileStorageService(tmp.toString());

        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> storage.resolve("../outside.txt"));

        assertEquals(500, e.getStatusCode().value());
    }

    @Test
    void treatsAMissingOrBlankKeyAsNoContent() {

        FileStorageService storage = new FileStorageService(tmp.toString());

        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> storage.resolve(null)).getStatusCode().value());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> storage.resolve("  ")).getStatusCode().value());
    }

    @Test
    void aPathLikeExtensionNeverReachesTheKey() throws IOException {

        FileStorageService storage = new FileStorageService(tmp.toString());

        String key = storage.store(upload("a.b/../../escape", "x"), 1);

        assertTrue(key.matches("1/[0-9a-f-]{36}"), key);
        assertTrue(Files.isRegularFile(storage.resolve(key)));
    }

    @Test
    void deleteRemovesTheBlobAndToleratesAlreadyMissingContent() throws IOException {

        FileStorageService storage = new FileStorageService(tmp.toString());

        String key = storage.store(upload("notes.txt", "hello"), 3);

        storage.delete(key);

        assertFalse(Files.exists(storage.resolve(key)));
        assertDoesNotThrow(() -> storage.delete("3/never-existed.txt"));
    }
}
