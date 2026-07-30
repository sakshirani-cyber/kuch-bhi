package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.dto.ExtractedContentResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ExcelFileParserTest {

    private final ExcelFileParser excelFileParser = new ExcelFileParser();

    @Test
    void testSupports() {
        assertTrue(excelFileParser.supports("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertTrue(excelFileParser.supports("xls", "application/vnd.ms-excel"));
        assertFalse(excelFileParser.supports("pdf", "application/pdf"));
    }

    @Test
    void testParse_ValidXlsx() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            Row header = sheet.createRow(0);
            Cell h1 = header.createCell(0);
            h1.setCellValue("ID");
            Cell h2 = header.createCell(1);
            h2.setCellValue("Name");

            Row dataRow = sheet.createRow(1);
            Cell d1 = dataRow.createCell(0);
            d1.setCellValue("101");
            Cell d2 = dataRow.createCell(1);
            d2.setCellValue("Preeti");

            workbook.write(out);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ExtractedContentResponse response = excelFileParser.parse(in);

        assertNotNull(response);
        assertEquals("excel", response.getFileType());
        assertEquals(1, response.getTotalCount());
        assertFalse(response.getExcelRows().isEmpty());
        assertEquals("101", response.getExcelRows().get(0).getCellData().get("ID"));
        assertEquals("Preeti", response.getExcelRows().get(0).getCellData().get("Name"));
    }
}
