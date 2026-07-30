package com.preeti.authenticationdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowDto {

    private int rowIndex;

    @Builder.Default
    private Map<String, String> cellData = new LinkedHashMap<>();

    public void addCell(String headerOrIndex, String cellValue) {
        if (cellData == null) {
            cellData = new LinkedHashMap<>();
        }
        cellData.put(headerOrIndex, cellValue);
    }
}
