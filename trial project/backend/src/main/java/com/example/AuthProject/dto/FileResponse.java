package com.example.AuthProject.dto;

import com.example.AuthProject.entity.ExtractionStatus;
import com.example.AuthProject.entity.UserFile;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileResponse {

    private Long id;
    private String originalFilename;
    private String contentType;
    private long sizeBytes;
    private ExtractionStatus extractionStatus;
    private String extractedText;
    private String errorMessage;
    private Instant createdAt;
    private String message;

    public static FileResponse from(UserFile file, boolean includeText) {
        return FileResponse.builder()
                .id(file.getId())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .sizeBytes(file.getSizeBytes())
                .extractionStatus(file.getExtractionStatus())
                .extractedText(includeText ? file.getExtractedText() : null)
                .errorMessage(file.getErrorMessage())
                .createdAt(file.getCreatedAt())
                .build();
    }

    public static FileResponse from(UserFile file, boolean includeText, String message) {
        FileResponse response = from(file, includeText);
        response.setMessage(message);
        return response;
    }
}
