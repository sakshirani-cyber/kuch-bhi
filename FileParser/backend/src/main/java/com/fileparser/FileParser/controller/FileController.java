package com.fileparser.FileParser.controller;

import com.fileparser.FileParser.dto.FileSummaryResponse;
import com.fileparser.FileParser.dto.RecordResponse;
import com.fileparser.FileParser.dto.UploadResponse;
import com.fileparser.FileParser.entity.ParsedRecord;
import com.fileparser.FileParser.entity.UploadedFile;
import com.fileparser.FileParser.repository.ParsedRecordRepository;
import com.fileparser.FileParser.repository.UploadedFileRepository;
import com.fileparser.FileParser.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;
    private final UploadedFileRepository uploadedFileRepository;
    private final ParsedRecordRepository parsedRecordRepository;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fileUploadService.upload(file));
    }

    @GetMapping
    public List<FileSummaryResponse> listFiles() {
        return uploadedFileRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/{id}/records")
    public Page<RecordResponse> getRecords(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (!uploadedFileRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        return parsedRecordRepository
                .findByUploadedFile_Id(id, PageRequest.of(page, size))
                .map(this::toRecord);
    }

    private FileSummaryResponse toSummary(UploadedFile file) {
        return FileSummaryResponse.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .fileType(file.getFileType())
                .status(file.getStatus())
                .rowCount(file.getRowCount())
                .columnCount(file.getColumnCount())
                .uploadedAt(file.getUploadedAt())
                .build();
    }

    private RecordResponse toRecord(ParsedRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .rowNumber(record.getRowNumber())
                .data(record.getData())
                .build();
    }
}
