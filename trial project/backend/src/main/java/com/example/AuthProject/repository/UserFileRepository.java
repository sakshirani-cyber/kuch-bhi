package com.example.AuthProject.repository;

import com.example.AuthProject.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UserFile, Long> {

    List<UserFile> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserFile> findByIdAndUserId(Long id, Long userId);
}
