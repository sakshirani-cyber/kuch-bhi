package com.example.files.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.files.entity.FileEntity;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    Page<FileEntity> findByUserIdOrderByUploadedAtDesc(String userId, Pageable pageable);

    @Transactional
    void deleteByUserId(String userId);
}
