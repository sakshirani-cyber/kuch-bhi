package com.fileparser.FileParser.service;

import com.fileparser.FileParser.detector.FileTypeDetector;
import com.fileparser.FileParser.dto.ParseResult;
import com.fileparser.FileParser.dto.UploadResponse;
import com.fileparser.FileParser.entity.UploadedFile;
import com.fileparser.FileParser.enums.FileType;
import com.fileparser.FileParser.parser.FileParser;
import com.fileparser.FileParser.parser.ParserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileTypeDetector fileTypeDetector;
    private final ParserFactory parserFactory;
    private final ImportService importService;

    public UploadResponse upload(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        FileType fileType = fileTypeDetector.detect(file);

        FileParser parser = parserFactory.getParser(fileType);

        ParseResult parseResult = parser.parse(file.getInputStream());

        UploadedFile uploadedFile = importService.saveImportedData(
                file.getOriginalFilename(),
                fileType,
                parseResult
        );

        return UploadResponse.builder()
                .id(uploadedFile.getId())
                .originalFileName(uploadedFile.getOriginalFileName())
                .fileType(uploadedFile.getFileType())
                .status(uploadedFile.getStatus())
                .rowCount(uploadedFile.getRowCount())
                .columnCount(uploadedFile.getColumnCount())
                .uploadedAt(uploadedFile.getUploadedAt())
                .message("File uploaded successfully.")
                .build();
    }
}
