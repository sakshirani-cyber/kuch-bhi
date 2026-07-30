package com.example.AuthProject.controller;

import com.example.AuthProject.dto.ApiResponse;
import com.example.AuthProject.dto.FileResponse;
import com.example.AuthProject.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        String email = authentication.getName();
        log.info("Upload request email={} originalFilename={}", email, file.getOriginalFilename());
        FileResponse body = fileService.upload(email, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, body.getMessage(), body));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FileResponse>>> list(Authentication authentication) {
        String email = authentication.getName();
        List<FileResponse> files = fileService.list(email);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Files retrieved", files));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getById(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        String email = authentication.getName();
        FileResponse file = fileService.getById(email, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "File retrieved", file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable("id") Long id
    ) {
        String email = authentication.getName();
        log.info("Delete file request email={} fileId={}", email, id);
        fileService.delete(email, id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "File deleted"));
    }
}
