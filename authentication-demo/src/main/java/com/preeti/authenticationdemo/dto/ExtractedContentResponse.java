package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedContentResponse {

    private String fileType;

    private int totalCount;

    private String rawText;

    @Builder.Default
    private List<ExcelRowDto> excelRows = new ArrayList<>();
}
