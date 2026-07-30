package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.dto.*;
import com.preeti.authenticationdemo.exception.*;
import com.preeti.authenticationdemo.model.ExtractedRecord;
import com.preeti.authenticationdemo.model.FileMetadata;
import com.preeti.authenticationdemo.parser.FileParser;
import com.preeti.authenticationdemo.parser.FileParserFactory;
import com.preeti.authenticationdemo.repository.ExtractedRecordRepository;
import com.preeti.authenticationdemo.repository.FileMetadataRepository;
import com.preeti.authenticationdemo.util.FileHashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileUploadService {

    private final FileStorageService fileStorageService;
    private final FileParserFactory fileParserFactory;
    private final FileMetadataRepository fileMetadataRepository;
    private final ExtractedRecordRepository extractedRecordRepository;
    private final Set<String> supportedExtensions;
    private final long maxUploadSizeBytes;

    public FileUploadService(FileStorageService fileStorageService,
                             FileParserFactory fileParserFactory,
                             FileMetadataRepository fileMetadataRepository,
                             ExtractedRecordRepository extractedRecordRepository,
                             @Value("${file.upload.supported-types:pdf,xlsx,xls}") String supportedTypes,
                             @Value("${file.upload.max-size:10485760}") long maxUploadSizeBytes) {
        this.fileStorageService = fileStorageService;
        this.fileParserFactory = fileParserFactory;
        this.fileMetadataRepository = fileMetadataRepository;
        this.extractedRecordRepository = extractedRecordRepository;
        this.supportedExtensions = Arrays.stream(supportedTypes.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        this.maxUploadSizeBytes = maxUploadSizeBytes;
    }

    public FileUploadResponse processAndSaveFile(MultipartFile file, String uploadedByUsername) {
        String username = (uploadedByUsername != null && !uploadedByUsername.isBlank()) ? uploadedByUsername : "ANONYMOUS";
        log.info("Processing upload request for file: '{}', uploaded by: '{}'",
                file != null ? file.getOriginalFilename() : "null", username);

        validateFile(file);

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception exception) {
            throw new FileStorageException("Failed to read file bytes: " + exception.getMessage(), exception);
        }

        String fileHash = FileHashUtil.calculateSha256(fileBytes);
        log.info("Calculated SHA-256 hash '{}' for file '{}'", fileHash, file.getOriginalFilename());

        Optional<FileMetadata> existingDuplicate = fileMetadataRepository.findByUploadedByAndFileHash(username, fileHash);
        if (existingDuplicate.isPresent()) {
            FileMetadata existing = existingDuplicate.get();
            log.info("Duplicate file detected! Reusing existing document ID: '{}'", existing.getId());
            existing.setLastViewedTimestamp(LocalDateTime.now());
            fileMetadataRepository.save(existing);

            return FileUploadResponse.builder()
                    .fileId(existing.getId())
                    .storedFilename(existing.getStoredFilename())
                    .originalFilename(existing.getOriginalFilename())
                    .fileExtension(existing.getFileExtension())
                    .fileSize(existing.getFileSize())
                    .uploadedBy(existing.getUploadedBy())
                    .isDuplicate(true)
                    .uploadStatus(existing.getUploadStatus() != null ? existing.getUploadStatus() : "COMPLETED")
                    .message("Duplicate file detected! Reused previously extracted data without re-processing.")
                    .extractedContent(getStoredExtractionResponse(existing))
                    .build();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        FileParser parser = fileParserFactory.getParser(extension, file.getContentType());

        ExtractedContentResponse extractedContent;
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            extractedContent = parser.parse(inputStream);
        } catch (Exception exception) {
            log.error("File extraction failed for file: '{}'", originalFilename, exception);
            throw new FileExtractionException("Error parsing document content: " + exception.getMessage(), exception);
        }

        FileStorageService.StoredFileResult storedResult = fileStorageService.storeFile(file);

        FileMetadata metadata = FileMetadata.builder()
                .fileHash(fileHash)
                .storedFilename(storedResult.storedFilename())
                .originalFilename(storedResult.originalFilename())
                .fileExtension(storedResult.fileExtension())
                .contentType(file.getContentType())
                .fileSize(storedResult.fileSize())
                .storagePath(storedResult.storagePath())
                .uploadTimestamp(LocalDateTime.now())
                .lastViewedTimestamp(LocalDateTime.now())
                .uploadedBy(username)
                .uploadStatus("COMPLETED")
                .extractionStatus("COMPLETED")
                .extractedRowCount(extractedContent.getTotalCount())
                .extractedTextPreview(getPreview(extractedContent))
                .fullExtractedText("pdf".equalsIgnoreCase(extension) ? extractedContent.getRawText() : null)
                .build();

        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

        if ("excel".equalsIgnoreCase(extractedContent.getFileType()) && extractedContent.getExcelRows() != null) {
            List<ExtractedRecord> recordsToSave = new ArrayList<>();
            for (ExcelRowDto rowDto : extractedContent.getExcelRows()) {
                String fullRowText = rowDto.getCellData() != null
                        ? String.join(" ", rowDto.getCellData().values())
                        : "";

                recordsToSave.add(ExtractedRecord.builder()
                        .fileId(savedMetadata.getId())
                        .uploadedBy(username)
                        .rowIndex(rowDto.getRowIndex())
                        .cellData(rowDto.getCellData())
                        .fullRowText(fullRowText)
                        .build());
            }
            extractedRecordRepository.saveAll(recordsToSave);
            log.info("Persisted {} Excel rows to MongoDB collection 'extracted_records'", recordsToSave.size());
        }

        return FileUploadResponse.builder()
                .fileId(savedMetadata.getId())
                .storedFilename(savedMetadata.getStoredFilename())
                .originalFilename(savedMetadata.getOriginalFilename())
                .fileExtension(savedMetadata.getFileExtension())
                .fileSize(savedMetadata.getFileSize())
                .uploadedBy(savedMetadata.getUploadedBy())
                .isDuplicate(false)
                .uploadStatus("COMPLETED")
                .message("File uploaded and parsed successfully.")
                .extractedContent(extractedContent)
                .build();
    }

    public DocumentContentResponseDto getDocumentContent(String fileId, String authenticatedUsername, int page, int size, String search) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found for ID: " + fileId));

        validateOwnership(metadata, authenticatedUsername);

        // Lazy Backfill / Re-Extraction for Legacy Files uploaded before persistent extraction
        ensureDocumentExtracted(metadata);

        metadata.setLastViewedTimestamp(LocalDateTime.now());
        fileMetadataRepository.save(metadata);

        if ("pdf".equalsIgnoreCase(metadata.getFileExtension())) {
            String text = metadata.getFullExtractedText() != null ? metadata.getFullExtractedText() : "";
            if (search != null && !search.isBlank()) {
                text = filterTextLines(text, search);
            }

            return DocumentContentResponseDto.builder()
                    .fileId(metadata.getId())
                    .originalFilename(metadata.getOriginalFilename())
                    .fileType("pdf")
                    .totalExtractedCount(metadata.getExtractedRowCount())
                    .rawText(text)
                    .build();
        }

        // Excel file server-side pagination & search
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rowIndex"));
        Page<ExtractedRecord> recordPage;

        if (search != null && !search.isBlank()) {
            recordPage = extractedRecordRepository.findByFileIdAndFullRowTextContainingIgnoreCase(fileId, search.trim(), pageable);
        } else {
            recordPage = extractedRecordRepository.findByFileId(fileId, pageable);
        }

        Page<ExcelRowDto> excelRowDtoPage = recordPage.map(rec -> ExcelRowDto.builder()
                .rowIndex(rec.getRowIndex())
                .cellData(rec.getCellData())
                .build());

        return DocumentContentResponseDto.builder()
                .fileId(metadata.getId())
                .originalFilename(metadata.getOriginalFilename())
                .fileType("excel")
                .totalExtractedCount((int) recordPage.getTotalElements())
                .paginatedExcelRows(excelRowDtoPage)
                .build();
    }

    private void ensureDocumentExtracted(FileMetadata metadata) {
        boolean isPdf = "pdf".equalsIgnoreCase(metadata.getFileExtension());
        boolean isPdfNeedsBackfill = isPdf && (metadata.getFullExtractedText() == null);
        
        long countExcelRecords = !isPdf ? extractedRecordRepository.findByFileId(metadata.getId(), PageRequest.of(0, 1)).getTotalElements() : 0;
        boolean isExcelNeedsBackfill = !isPdf && (countExcelRecords == 0);

        if (isPdfNeedsBackfill || isExcelNeedsBackfill) {
            log.info("Lazy extraction triggered for legacy file ID: '{}', name: '{}'", metadata.getId(), metadata.getOriginalFilename());
            try {
                Resource resource = fileStorageService.loadAsResource(metadata.getStoredFilename());
                FileParser parser = fileParserFactory.getParser(metadata.getFileExtension(), metadata.getContentType());
                
                try (InputStream is = resource.getInputStream()) {
                    ExtractedContentResponse response = parser.parse(is);
                    metadata.setExtractedRowCount(response.getTotalCount());
                    metadata.setUploadStatus("COMPLETED");
                    metadata.setExtractionStatus("COMPLETED");

                    if (isPdf) {
                        metadata.setFullExtractedText(response.getRawText());
                    } else if (response.getExcelRows() != null) {
                        List<ExtractedRecord> recordsToSave = new ArrayList<>();
                        for (ExcelRowDto rowDto : response.getExcelRows()) {
                            String fullRowText = rowDto.getCellData() != null ? String.join(" ", rowDto.getCellData().values()) : "";
                            recordsToSave.add(ExtractedRecord.builder()
                                    .fileId(metadata.getId())
                                    .uploadedBy(metadata.getUploadedBy())
                                    .rowIndex(rowDto.getRowIndex())
                                    .cellData(rowDto.getCellData())
                                    .fullRowText(fullRowText)
                                    .build());
                        }
                        extractedRecordRepository.saveAll(recordsToSave);
                    }
                    fileMetadataRepository.save(metadata);
                }
            } catch (Exception ex) {
                log.error("Failed lazy backfill extraction for file ID: '{}'", metadata.getId(), ex);
            }
        }
    }

    public Page<FileMetadataDto> getPaginatedFiles(String authenticatedUsername, int page, int size, String sortBy, String sortDirection, String search) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy != null ? sortBy : "uploadTimestamp"));

        Page<FileMetadata> metadataPage;
        if (authenticatedUsername != null && !authenticatedUsername.isBlank() && !"ANONYMOUS".equalsIgnoreCase(authenticatedUsername)) {
            if (search != null && !search.isBlank()) {
                metadataPage = fileMetadataRepository.findByUploadedByAndOriginalFilenameContainingIgnoreCase(authenticatedUsername, search.trim(), pageable);
            } else {
                metadataPage = fileMetadataRepository.findByUploadedBy(authenticatedUsername, pageable);
            }
        } else {
            // Fallback: If anonymous/session user, return all metadata
            metadataPage = fileMetadataRepository.findAll(pageable);
        }

        return metadataPage.map(this::mapToDto);
    }

    public FileMetadataDto getFileById(String id, String authenticatedUsername) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found for ID: " + id));

        validateOwnership(metadata, authenticatedUsername);
        return mapToDto(metadata);
    }

    public void deleteFileById(String id, String authenticatedUsername) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found for ID: " + id));

        validateOwnership(metadata, authenticatedUsername);

        fileStorageService.deleteFile(metadata.getStoredFilename());
        extractedRecordRepository.deleteByFileId(id);
        fileMetadataRepository.deleteById(id);
        log.info("Deleted physical file, metadata, and extracted records for ID: '{}'", id);
    }

    private void validateOwnership(FileMetadata metadata, String authenticatedUsername) {
        if (authenticatedUsername != null && !authenticatedUsername.isBlank() && !"ANONYMOUS".equalsIgnoreCase(authenticatedUsername)) {
            if (!authenticatedUsername.equalsIgnoreCase(metadata.getUploadedBy())) {
                log.warn("User '{}' attempted unauthorized access to file ID '{}' belonging to '{}'",
                        authenticatedUsername, metadata.getId(), metadata.getUploadedBy());
                throw new UnauthorizedAccessException("You do not have permission to access this document");
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EmptyFileException("Uploaded file is empty or missing");
        }

        if (file.getSize() > maxUploadSizeBytes) {
            throw new InvalidFileTypeException("File size exceeds maximum allowed limit of " + (maxUploadSizeBytes / (1024 * 1024)) + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        if (extension.isEmpty() || !supportedExtensions.contains(extension)) {
            log.warn("Invalid file extension '{}'. Supported extensions: {}", extension, supportedExtensions);
            throw new InvalidFileTypeException("Unsupported file type: ." + extension + ". Allowed formats: " + supportedExtensions);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }

    private String getPreview(ExtractedContentResponse extractedContent) {
        if (extractedContent == null) return "";
        if (extractedContent.getRawText() != null && !extractedContent.getRawText().isEmpty()) {
            String raw = extractedContent.getRawText();
            return raw.length() > 200 ? raw.substring(0, 200) + "..." : raw;
        }
        if (extractedContent.getExcelRows() != null && !extractedContent.getExcelRows().isEmpty()) {
            return "Excel Sheet containing " + extractedContent.getExcelRows().size() + " data rows";
        }
        return "";
    }

    private ExtractedContentResponse getStoredExtractionResponse(FileMetadata metadata) {
        if ("pdf".equalsIgnoreCase(metadata.getFileExtension())) {
            return ExtractedContentResponse.builder()
                    .fileType("pdf")
                    .totalCount(metadata.getExtractedRowCount())
                    .rawText(metadata.getFullExtractedText())
                    .build();
        }
        return ExtractedContentResponse.builder()
                .fileType("excel")
                .totalCount(metadata.getExtractedRowCount())
                .build();
    }

    private String filterTextLines(String text, String keyword) {
        if (text == null || text.isBlank() || keyword == null || keyword.isBlank()) return text;
        String lowerKey = keyword.toLowerCase();
        return Arrays.stream(text.split("\n"))
                .filter(line -> line.toLowerCase().contains(lowerKey))
                .collect(Collectors.joining("\n"));
    }

    private FileMetadataDto mapToDto(FileMetadata entity) {
        String uploadStatus = entity.getUploadStatus() != null ? entity.getUploadStatus() : "COMPLETED";
        String extractionStatus = entity.getExtractionStatus() != null ? entity.getExtractionStatus() : "COMPLETED";
        String uploadedBy = entity.getUploadedBy() != null ? entity.getUploadedBy() : "ANONYMOUS";

        return FileMetadataDto.builder()
                .id(entity.getId())
                .fileHash(entity.getFileHash())
                .storedFilename(entity.getStoredFilename())
                .originalFilename(entity.getOriginalFilename())
                .fileExtension(entity.getFileExtension())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .uploadTimestamp(entity.getUploadTimestamp())
                .lastViewedTimestamp(entity.getLastViewedTimestamp())
                .uploadedBy(uploadedBy)
                .uploadStatus(uploadStatus)
                .extractionStatus(extractionStatus)
                .extractedRowCount(entity.getExtractedRowCount())
                .extractedTextPreview(entity.getExtractedTextPreview())
                .build();
    }
}
