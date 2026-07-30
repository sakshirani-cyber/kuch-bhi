package com.example.files.controller;

import com.example.files.dto.FileMetaDto;
import com.example.files.entity.FileEntity;
import com.example.files.service.FileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            @AuthenticationPrincipal OAuth2User user) {
        try {
            String userId = getUserId(session, user);
            boolean guest = (user == null);
            FileMetaDto dto = fileService.uploadFile(file, userId, guest);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public Page<FileMetaDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session,
            @AuthenticationPrincipal OAuth2User user) {
        String userId = getUserId(session, user);
        return fileService.getFiles(userId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        FileEntity file = fileService.getFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .body(file.getContent());
    }

    @GetMapping("/{id}/preview")
    public Map<String, Object> preview(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return fileService.previewFile(id, page, size);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    private String getUserId(HttpSession session, OAuth2User user) {
        if (user != null) {
            return "github_" + user.getAttribute("id");
        }
        String guestId = (String) session.getAttribute("guestId");
        if (guestId == null) {
            guestId = "guest_" + UUID.randomUUID();
            session.setAttribute("guestId", guestId);
        }
        return guestId;
    }
}
