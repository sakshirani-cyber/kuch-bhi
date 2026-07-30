package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.exception.EmptyFileException;
import com.preeti.authenticationdemo.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
        fileStorageService.initDirectory();
    }

    @Test
    void testStoreFile_Success() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "sample.pdf",
                "application/pdf",
                "Hello PDF Content".getBytes()
        );

        FileStorageService.StoredFileResult result = fileStorageService.storeFile(mockFile);

        assertNotNull(result);
        assertEquals("sample.pdf", result.originalFilename());
        assertEquals("pdf", result.fileExtension());
        assertTrue(result.storedFilename().endsWith("sample.pdf"));
        assertTrue(result.fileSize() > 0);
    }

    @Test
    void testStoreFile_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(EmptyFileException.class, () -> fileStorageService.storeFile(emptyFile));
    }

    @Test
    void testStoreFile_PathTraversal_ThrowsException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "../secret.txt",
                "text/plain",
                "data".getBytes()
        );

        assertThrows(FileStorageException.class, () -> fileStorageService.storeFile(invalidFile));
    }
}
