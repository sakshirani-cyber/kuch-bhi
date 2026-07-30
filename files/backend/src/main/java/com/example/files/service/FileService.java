package com.example.files.service;

import com.example.files.dto.FileMetaDto;
import com.example.files.entity.FileEntity;
import com.example.files.repository.FileRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".csv", ".xlsx", ".xls", ".txt");

    @Autowired
    private FileRepository fileRepository;

    public FileMetaDto uploadFile(MultipartFile file, String userId, boolean guest) throws Exception {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (ALLOWED_EXTENSIONS.stream().noneMatch(name::endsWith)) {
            throw new IllegalArgumentException("Unsupported file type. Only CSV, Excel, and TXT files are allowed.");
        }

        FileEntity entity = FileEntity.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .content(file.getBytes())
                .uploadedAt(LocalDateTime.now())
                .userId(userId)
                .guest(guest)
                .build();
        entity = fileRepository.save(entity);
        return toDto(entity);
    }

    public Page<FileMetaDto> getFiles(String userId, Pageable pageable) {
        return fileRepository.findByUserIdOrderByUploadedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    public FileEntity getFile(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void deleteFile(UUID id) {
        fileRepository.deleteById(id);
    }

    @Transactional
    public void deleteGuestFiles(String userId) {
        fileRepository.deleteByUserId(userId);
    }

    public Map<String, Object> previewFile(UUID id, int page, int size) {
        FileEntity file = getFile(id);
        String name = file.getFileName().toLowerCase();

        Map<String, Object> result;
        if (name.endsWith(".csv")) {
            result = parseCsv(file.getContent(), page, size);
        } else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            result = parseExcel(file.getContent(), page, size);
        } else if (name.endsWith(".txt")) {
            result = parseText(file.getContent(), page, size);
        } else {
            result = new HashMap<>();
            result.put("error", "Unsupported file type for preview.");
        }
        result.put("fileName", file.getFileName());
        return result;
    }


    private Map<String, Object> parseCsv(byte[] content, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "table");
        try {
            Reader reader = new InputStreamReader(new ByteArrayInputStream(content));
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            try (CSVParser parser = format.parse(reader)) {
                List<String> headers = parser.getHeaderNames();
                List<CSVRecord> allRecords = parser.getRecords();

                int totalRows = allRecords.size();
                int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
                int start = page * size;
                int end = Math.min(start + size, totalRows);

                List<List<String>> rows = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    List<String> row = new ArrayList<>();
                    for (String val : allRecords.get(i)) {
                        row.add(val);
                    }
                    rows.add(row);
                }

                result.put("headers", headers);
                result.put("rows", rows);
                result.put("currentPage", page);
                result.put("totalPages", totalPages);
                result.put("totalRows", totalRows);
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> parseExcel(byte[] content, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "table");
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headers.add(getCellValue(cell));
                }
            }

            int totalRows = sheet.getLastRowNum(); // excludes header row
            int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / size));
            int start = page * size + 1; // +1 to skip header
            int end = Math.min(start + size, totalRows + 1);

            List<List<String>> rows = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Row row = sheet.getRow(i);
                List<String> rowData = new ArrayList<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = (row != null) ? row.getCell(j) : null;
                    rowData.add(cell != null ? getCellValue(cell) : "");
                }
                rows.add(rowData);
            }

            result.put("headers", headers);
            result.put("rows", rows);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalRows", totalRows);
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> parseText(byte[] content, int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "text");
        try {
            String[] lines = new String(content).split("\n");
            int totalLines = lines.length;
            int totalPages = Math.max(1, (int) Math.ceil((double) totalLines / size));
            int start = page * size;
            int end = Math.min(start + size, totalLines);

            StringBuilder pageText = new StringBuilder();
            for (int i = start; i < end; i++) {
                pageText.append(lines[i]).append("\n");
            }

            result.put("content", pageText.toString());
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalLines", totalLines);
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }


    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private FileMetaDto toDto(FileEntity entity) {
        return new FileMetaDto(
                entity.getId(),
                entity.getFileName(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getUploadedAt()
        );
    }
}
