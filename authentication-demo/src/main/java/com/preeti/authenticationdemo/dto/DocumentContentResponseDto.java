package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContentResponseDto {

    private String fileId;

    private String originalFilename;

    private String fileType;

    private int totalExtractedCount;

    private String rawText;

    private Page<ExcelRowDto> paginatedExcelRows;
}
