package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.dto.ExcelRowDto;
import com.preeti.authenticationdemo.dto.ExtractedContentResponse;
import com.preeti.authenticationdemo.exception.FileExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelFileParser implements FileParser {

    @Override
    public boolean supports(String fileExtension, String contentType) {
        if (fileExtension != null) {
            String ext = fileExtension.toLowerCase();
            if (ext.equals("xlsx") || ext.equals("xls")) {
                return true;
            }
        }
        if (contentType != null) {
            String type = contentType.toLowerCase();
            return type.contains("spreadsheet") || type.contains("excel") || type.contains("ms-excel");
        }
        return false;
    }

    @Override
    public ExtractedContentResponse parse(InputStream inputStream) {
        log.info("Excel extraction started");
        List<ExcelRowDto> rows = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return ExtractedContentResponse.builder()
                        .fileType("excel")
                        .totalCount(0)
                        .excelRows(rows)
                        .build();
            }

            List<String> headers = new ArrayList<>();
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isRowEmpty(row, dataFormatter)) {
                    continue;
                }

                if (isFirstRow) {
                    for (Cell cell : row) {
                        headers.add(dataFormatter.formatCellValue(cell).trim());
                    }
                    isFirstRow = false;
                    continue;
                }

                ExcelRowDto rowDto = ExcelRowDto.builder()
                        .rowIndex(row.getRowNum())
                        .build();

                int cellCount = Math.max(headers.size(), row.getLastCellNum());
                for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    String value = cell != null ? dataFormatter.formatCellValue(cell).trim() : "";
                    String headerName = (cellIndex < headers.size() && !headers.get(cellIndex).isEmpty())
                            ? headers.get(cellIndex)
                            : "Column_" + (cellIndex + 1);

                    rowDto.addCell(headerName, value);
                }

                rows.add(rowDto);
            }

            log.info("Excel extraction completed. Extracted {} data rows", rows.size());

            return ExtractedContentResponse.builder()
                    .fileType("excel")
                    .totalCount(rows.size())
                    .excelRows(rows)
                    .build();

        } catch (Exception exception) {
            log.error("Failed to parse Excel workbook", exception);
            throw new FileExtractionException("Could not extract rows/cells from Excel file: " + exception.getMessage(), exception);
        }
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = formatter.formatCellValue(cell);
                if (val != null && !val.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
