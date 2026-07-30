package com.fileparser.FileParser.repository;

import com.fileparser.FileParser.entity.ParsedRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParsedRecordRepository extends JpaRepository<ParsedRecord, Long> {

    Page<ParsedRecord> findByUploadedFile_Id(
            UUID uploadedFileId,
            Pageable pageable);

}
