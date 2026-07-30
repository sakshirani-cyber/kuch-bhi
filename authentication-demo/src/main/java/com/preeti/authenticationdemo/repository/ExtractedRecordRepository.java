package com.preeti.authenticationdemo.repository;

import com.preeti.authenticationdemo.model.ExtractedRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtractedRecordRepository extends MongoRepository<ExtractedRecord, String> {

    Page<ExtractedRecord> findByFileId(String fileId, Pageable pageable);

    Page<ExtractedRecord> findByFileIdAndFullRowTextContainingIgnoreCase(String fileId, String searchKey, Pageable pageable);

    void deleteByFileId(String fileId);
}
