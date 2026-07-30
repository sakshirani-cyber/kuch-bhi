package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private String fileId;

    private String storedFilename;

    private String originalFilename;

    private String fileExtension;

    private long fileSize;

    private String uploadedBy;

    private boolean isDuplicate;

    private String uploadStatus;

    private String message;

    private ExtractedContentResponse extractedContent;
}
