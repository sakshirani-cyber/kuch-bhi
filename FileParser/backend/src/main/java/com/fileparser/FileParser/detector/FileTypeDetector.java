package com.fileparser.FileParser.detector;

import com.fileparser.FileParser.enums.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileTypeDetector {

    public FileType detect(MultipartFile file) {

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("Filename cannot be null.");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase();

        return switch (extension) {
            case "xlsx", "xls" -> FileType.EXCEL;
            case "csv" -> FileType.CSV;
            case "txt" -> FileType.TXT;
            case "pdf" -> FileType.PDF;
            default -> throw new IllegalArgumentException(
                    "Unsupported file type: " + extension
            );
        };
    }
}
