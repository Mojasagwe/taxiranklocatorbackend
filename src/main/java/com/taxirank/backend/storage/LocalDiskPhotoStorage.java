package com.taxirank.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** Photos as files in one directory (app.storage.photos-dir), named by random key */
@Component
public class LocalDiskPhotoStorage implements PhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalDiskPhotoStorage.class);

    // Keys come from the database, but never let one escape the directory
    private static final Pattern KEY = Pattern.compile("^[0-9a-f-]{36}\\.jpg$");

    private final Path directory;

    public LocalDiskPhotoStorage(@Value("${app.storage.photos-dir}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public String save(byte[] jpeg) {
        String key = UUID.randomUUID() + ".jpg";
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(key), jpeg);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store photo", e);
        }
        return key;
    }

    @Override
    public Optional<byte[]> load(String key) {
        Path file = resolve(key);
        if (file == null || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read photo", e);
        }
    }

    @Override
    public void delete(String key) {
        Path file = resolve(key);
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // An orphaned file is harmless; don't fail the user's request over it
            log.warn("Could not delete photo {}", key, e);
        }
    }

    private Path resolve(String key) {
        return key != null && KEY.matcher(key).matches() ? directory.resolve(key) : null;
    }
}
