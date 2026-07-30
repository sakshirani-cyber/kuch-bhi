package com.fileparser.FileParser.parser;

import com.fileparser.FileParser.dto.ParseResult;
import com.fileparser.FileParser.dto.ParsedRow;
import com.fileparser.FileParser.enums.FileType;
import com.fileparser.FileParser.util.ExcelCellUtil;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelParser implements FileParser {

    @Override
    public FileType supportedType() {
        return FileType.EXCEL;
    }

    @Override
    public ParseResult parse(InputStream inputStream) throws Exception {

        List<String> headers = new ArrayList<>();
        List<ParsedRow> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Read Header Row
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file is empty.");
            }

            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            // Read Data Rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row currentRow = sheet.getRow(i);

                if (currentRow == null) {
                    continue;
                }

                Map<String, Object> values = new LinkedHashMap<>();

                for (int j = 0; j < headers.size(); j++) {

                    Cell cell = currentRow.getCell(j);

                    values.put(
                            headers.get(j),
                            ExcelCellUtil.getCellValue(cell)
                    );
                }

                rows.add(
                        ParsedRow.builder()
                                .rowNumber(i)
                                .values(values)
                                .build()
                );
            }
        }

        return ParseResult.builder()
                .headers(headers)
                .rows(rows)
                .build();
    }
}