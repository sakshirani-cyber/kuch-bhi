package com.example.files.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaDto {
    private UUID id;

	
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}
