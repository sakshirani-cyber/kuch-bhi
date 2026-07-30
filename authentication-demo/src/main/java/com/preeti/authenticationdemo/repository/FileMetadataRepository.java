package com.preeti.authenticationdemo.repository;

import com.preeti.authenticationdemo.model.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {

    Optional<FileMetadata> findByUploadedByAndFileHash(String uploadedBy, String fileHash);

    Page<FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);

    Page<FileMetadata> findByUploadedByAndOriginalFilenameContainingIgnoreCase(String uploadedBy, String keyword, Pageable pageable);

    Optional<FileMetadata> findByStoredFilename(String storedFilename);
}
