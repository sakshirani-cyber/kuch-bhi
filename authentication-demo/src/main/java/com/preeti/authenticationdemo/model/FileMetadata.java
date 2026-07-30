package com.preeti.authenticationdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_metadata")
public class FileMetadata {

    @Id
    private String id;

    @Indexed
    private String fileHash;

    private String storedFilename;

    private String originalFilename;

    private String fileExtension;

    private String contentType;

    private long fileSize;

    private String storagePath;

    private LocalDateTime uploadTimestamp;

    private LocalDateTime lastViewedTimestamp;

    @Indexed
    private String uploadedBy;

    @Builder.Default
    private String uploadStatus = "COMPLETED";

    @Builder.Default
    private String extractionStatus = "COMPLETED";

    private int extractedRowCount;

    private String extractedTextPreview;

    private String fullExtractedText;
}
