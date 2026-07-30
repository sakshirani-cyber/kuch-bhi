package com.fileparser.FileParser.service;

import com.fileparser.FileParser.dto.ParseResult;
import com.fileparser.FileParser.dto.ParsedRow;
import com.fileparser.FileParser.entity.ParsedRecord;
import com.fileparser.FileParser.entity.UploadedFile;
import com.fileparser.FileParser.enums.FileType;
import com.fileparser.FileParser.enums.UploadStatus;
import com.fileparser.FileParser.repository.UploadedFileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ImportService {

    private final UploadedFileRepository uploadedFileRepository;

    public UploadedFile saveImportedData(
            String originalFileName,
            FileType fileType,
            ParseResult parseResult
    ) {

        UploadedFile uploadedFile = UploadedFile.builder()
                .originalFileName(originalFileName)
                .fileType(fileType)
                .status(UploadStatus.COMPLETED)
                .rowCount(parseResult.getRows().size())
                .columnCount(parseResult.getHeaders().size())
                .uploadedAt(LocalDateTime.now())
                .build();

        for (ParsedRow row : parseResult.getRows()) {

            ParsedRecord parsedRecord = ParsedRecord.builder()
                    .rowNumber(row.getRowNumber())
                    .data(row.getValues())
                    .uploadedFile(uploadedFile)
                    .build();

            uploadedFile.getRecords().add(parsedRecord);
        }

        return uploadedFileRepository.save(uploadedFile);
    }
}
