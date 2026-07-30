package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.exception.EmptyFileException;
import com.preeti.authenticationdemo.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload.directory:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initDirectory() {
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Initialized upload storage directory at '{}'", this.fileStorageLocation);
        } catch (Exception exception) {
            log.error("Could not create storage directory at '{}'", this.fileStorageLocation, exception);
            throw new FileStorageException("Could not create upload directory", exception);
        }
    }

    public StoredFileResult storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EmptyFileException("Cannot store an empty or missing file");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            log.warn("Filename contains invalid path sequence: '{}'", originalFilename);
            throw new FileStorageException("Filename contains invalid path traversal sequence: " + originalFilename);
        }

        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully stored file '{}' as '{}'", originalFilename, storedFilename);
            return new StoredFileResult(storedFilename, originalFilename, fileExtension, targetLocation.toString(), file.getSize());
        } catch (IOException exception) {
            log.error("Failed to store file '{}'", originalFilename, exception);
            throw new FileStorageException("Could not store file " + originalFilename + ". Please try again!", exception);
        }
    }

    public Path getFilePath(String storedFilename) {
        return this.fileStorageLocation.resolve(storedFilename).normalize();
    }

    public Resource loadAsResource(String storedFilename) {
        try {
            Path filePath = getFilePath(storedFilename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or unreadable: " + storedFilename);
            }
        } catch (MalformedURLException exception) {
            throw new FileStorageException("File not found: " + storedFilename, exception);
        }
    }

    public void deleteFile(String storedFilename) {
        try {
            Path filePath = getFilePath(storedFilename);
            Files.deleteIfExists(filePath);
            log.info("Deleted physical file '{}' from storage", storedFilename);
        } catch (IOException exception) {
            log.warn("Failed to delete physical file '{}': {}", storedFilename, exception.getMessage());
        }
    }

    public record StoredFileResult(
            String storedFilename,
            String originalFilename,
            String fileExtension,
            String storagePath,
            long fileSize
    ) {}
}
