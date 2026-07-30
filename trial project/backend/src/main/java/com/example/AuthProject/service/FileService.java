package com.example.AuthProject.service;

import com.example.AuthProject.dto.FileResponse;
import com.example.AuthProject.entity.User;
import com.example.AuthProject.entity.UserFile;
import com.example.AuthProject.exception.ApiException;
import com.example.AuthProject.repository.UserFileRepository;
import com.example.AuthProject.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;
    private final FileExtractionService extractionService;
    private final Path uploadRoot;
    private final DataSize maxFileSize;

    public FileService(
            UserRepository userRepository,
            UserFileRepository userFileRepository,
            FileExtractionService extractionService,
            @Value("${app.files.upload-dir:uploads}") String uploadDir,
            @Value("${app.files.max-size:10MB}") DataSize maxFileSize
    ) throws IOException {
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
        this.extractionService = extractionService;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        Files.createDirectories(this.uploadRoot);
        log.info("File upload directory ready path={} maxSize={}", this.uploadRoot, maxFileSize);
    }

    @Transactional
    public FileResponse upload(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "File is required",
                    Map.of("file", "Please select a non-empty file")
            );
        }

        User user = requireUser(email);
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        long size = file.getSize();

        if (size > maxFileSize.toBytes()) {
            throw new ApiException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "File upload failed",
                    Map.of("file", "File exceeds the maximum allowed size of " + formatDataSize(maxFileSize))
            );
        }

        log.info("File received userId={} filename={} contentType={} sizeBytes={}",
                user.getUserId(), originalFilename, contentType, size);

        Path userDir = uploadRoot.resolve(String.valueOf(user.getUserId()));
        Path target;
        try {
            Files.createDirectories(userDir);
            String storedName = UUID.randomUUID() + "_" + originalFilename;
            target = userDir.resolve(storedName).normalize();
            if (!target.startsWith(userDir)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid filename",
                        Map.of("file", "Filename is not allowed")
                );
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to disk userId={} path={} sizeBytes={}",
                    user.getUserId(), target, Files.size(target));
        } catch (ApiException ex) {
            throw ex;
        } catch (IOException e) {
            log.warn("Failed to store uploaded file userId={} filename={} reason={}",
                    user.getUserId(), originalFilename, e.getMessage());
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file",
                    Map.of("file", "Could not save uploaded file")
            );
        }

        FileExtractionService.ExtractionResult extraction =
                extractionService.extract(target, contentType, originalFilename);

        UserFile entity = new UserFile();
        entity.setUserId(user.getUserId());
        entity.setOriginalFilename(originalFilename);
        entity.setContentType(contentType);
        entity.setSizeBytes(size);
        entity.setStoragePath(target.toString());
        entity.setExtractedText(extraction.text());
        entity.setExtractionStatus(extraction.status());
        entity.setErrorMessage(extraction.errorMessage());

        UserFile saved = userFileRepository.save(entity);
        log.info("File metadata saved fileId={} userId={} status={}",
                saved.getId(), user.getUserId(), saved.getExtractionStatus());

        String message = switch (saved.getExtractionStatus()) {
            case EXTRACTED -> extraction.truncated()
                    ? "File uploaded; text extracted (truncated to max length)"
                    : "File uploaded; text extracted successfully";
            case EMPTY -> "File uploaded; no extractable text found";
            case FAILED -> "File uploaded; text extraction failed";
        };

        return FileResponse.from(saved, true, message);
    }

    @Transactional(readOnly = true)
    public List<FileResponse> list(String email) {
        User user = requireUser(email);
        return userFileRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId()).stream()
                .map(file -> FileResponse.from(file, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public FileResponse getById(String email, Long fileId) {
        User user = requireUser(email);
        UserFile file = requireOwnedFile(user.getUserId(), fileId);
        return FileResponse.from(file, true);
    }

    @Transactional
    public void delete(String email, Long fileId) {
        User user = requireUser(email);
        UserFile file = requireOwnedFile(user.getUserId(), fileId);

        userFileRepository.delete(file);
        log.info("File metadata deleted fileId={} userId={}", fileId, user.getUserId());

        try {
            Path stored = Path.of(file.getStoragePath()).normalize();
            if (stored.startsWith(uploadRoot) && Files.exists(stored)) {
                Files.deleteIfExists(stored);
                log.info("File removed from disk path={}", stored);
            } else {
                log.warn("Skipped disk delete for unsafe or missing path fileId={} path={}",
                        fileId, file.getStoragePath());
            }
        } catch (IOException e) {
            log.warn("Failed to delete file from disk fileId={} path={} reason={}",
                    fileId, file.getStoragePath(), e.getMessage());
        }
    }

    private UserFile requireOwnedFile(Long userId, Long fileId) {
        return userFileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "File not found",
                        Map.of("file", "No file found with id " + fileId)
                ));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        Map.of("email", "No account found for this email")
                ));
    }

    private static String sanitizeFilename(String original) {
        String name = (original == null || original.isBlank()) ? "upload.bin" : original.trim();
        name = name.replace("\\", "_").replace("/", "_");
        name = name.replaceAll("[^a-zA-Z0-9._\\- ()]", "_");
        if (name.length() > 180) {
            name = name.substring(name.length() - 180);
        }
        return name;
    }

    private static String formatDataSize(DataSize size) {
        long bytes = size.toBytes();
        if (bytes % (1024L * 1024L) == 0) {
            return (bytes / (1024L * 1024L)) + " MB";
        }
        if (bytes % 1024L == 0) {
            return (bytes / 1024L) + " KB";
        }
        return bytes + " bytes";
    }
}
