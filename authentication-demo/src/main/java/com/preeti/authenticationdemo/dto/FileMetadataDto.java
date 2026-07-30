package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataDto {

    private String id;

    private String fileHash;

    private String storedFilename;

    private String originalFilename;

    private String fileExtension;

    private String contentType;

    private long fileSize;

    private LocalDateTime uploadTimestamp;

    private LocalDateTime lastViewedTimestamp;

    private String uploadedBy;

    private String uploadStatus;

    private String extractionStatus;

    private int extractedRowCount;

    private String extractedTextPreview;
}
