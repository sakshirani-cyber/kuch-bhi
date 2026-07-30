package com.preeti.authenticationdemo.controller;

import com.preeti.authenticationdemo.dto.ApiResponse;
import com.preeti.authenticationdemo.dto.DocumentContentResponseDto;
import com.preeti.authenticationdemo.dto.FileMetadataDto;
import com.preeti.authenticationdemo.dto.FileUploadResponse;
import com.preeti.authenticationdemo.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Document Management & Data Extraction Module", description = "APIs for uploading, SHA-256 duplicate detection, persistent document storage, reopening extracted content, and server-side searching/pagination")
public class FileController {

    private final FileUploadService fileUploadService;

    public FileController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload & Parse Document", description = "Uploads a PDF (.pdf) or Excel (.xlsx, .xls) file. Calculates SHA-256 hash to detect duplicates. If new, extracts content once using Strategy Pattern and permanently indexes it in MongoDB.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "File uploaded and parsed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file type, empty file, or validation failure"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate file detected - reused previously extracted data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "Payload too large - file size exceeds limit")
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "Multipart file to upload (PDF, XLSX, XLS)", required = true)
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        String username = getAuthenticatedUsername(request);
        log.info("REST request to upload document: '{}' by user: '{}'", file.getOriginalFilename(), username);

        FileUploadResponse response = fileUploadService.processAndSaveFile(file, username);
        HttpStatus status = response.isDuplicate() ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(status)
                .body(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get User Document History", description = "Retrieves user's uploaded document history with pagination, sorting, and filename keyword searching.")
    public ResponseEntity<ApiResponse<Page<FileMetadataDto>>> getPaginatedFiles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "uploadTimestamp") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(value = "search", required = false) String search,
            HttpServletRequest request) {

        String username = getAuthenticatedUsername(request);
        Page<FileMetadataDto> pageResult = fileUploadService.getPaginatedFiles(username, page, size, sortBy, sortDirection, search);
        return ResponseEntity.ok(ApiResponse.success("Retrieved document history successfully", pageResult));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Document Metadata by ID", description = "Retrieves document metadata and status for the specified document ID.")
    public ResponseEntity<ApiResponse<FileMetadataDto>> getFileById(
            @Parameter(description = "MongoDB File Metadata ID", required = true)
            @PathVariable("id") String id,
            HttpServletRequest request) {

        String username = getAuthenticatedUsername(request);
        FileMetadataDto file = fileUploadService.getFileById(id, username);
        return ResponseEntity.ok(ApiResponse.success("Retrieved document metadata successfully", file));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Reopen & Search Document Content", description = "Reopens a previously uploaded document and retrieves its stored extracted data with server-side pagination and keyword searching without re-extracting.")
    public ResponseEntity<ApiResponse<DocumentContentResponseDto>> getDocumentContent(
            @Parameter(description = "MongoDB File Metadata ID", required = true)
            @PathVariable("id") String id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            HttpServletRequest request) {

        String username = getAuthenticatedUsername(request);
        log.info("REST request to reopen document content for ID: '{}' by user: '{}'", id, username);

        DocumentContentResponseDto content = fileUploadService.getDocumentContent(id, username, page, size, search);
        return ResponseEntity.ok(ApiResponse.success("Reopened document content successfully", content));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Document", description = "Deletes physical file from disk storage and removes all metadata and extracted records from MongoDB.")
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @Parameter(description = "MongoDB File Metadata ID", required = true)
            @PathVariable("id") String id,
            HttpServletRequest request) {

        String username = getAuthenticatedUsername(request);
        fileUploadService.deleteFileById(id, username);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
    }

    private String getAuthenticatedUsername(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        if (request != null) {
            String headerUser = request.getHeader("X-User-Name");
            if (headerUser != null && !headerUser.isBlank()) {
                return headerUser;
            }
        }
        return "ANONYMOUS";
    }
}
