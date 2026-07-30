package com.fileparser.FileParser.dto;

import com.fileparser.FileParser.enums.FileType;
import com.fileparser.FileParser.enums.UploadStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResponse {

    private UUID id;

    private String originalFileName;

    private FileType fileType;

    private UploadStatus status;

    private Integer rowCount;

    private Integer columnCount;

    private LocalDateTime uploadedAt;

    private String message;
}
