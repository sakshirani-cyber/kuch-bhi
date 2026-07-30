package com.example.AuthProject.service;

import com.example.AuthProject.entity.ExtractionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class FileExtractionService {

    private final Tika tika = new Tika();
    private final int maxExtractedChars;

    public FileExtractionService(
            @Value("${app.files.max-extracted-chars:100000}") int maxExtractedChars
    ) {
        this.maxExtractedChars = maxExtractedChars;
        // Cap Tika string length roughly to our stored max (+ small buffer for detection)
        this.tika.setMaxStringLength(Math.max(maxExtractedChars, 1));
    }

    public ExtractionResult extract(Path filePath, String contentType, String originalFilename) {
        long started = System.currentTimeMillis();
        log.info("Extraction started filename={} contentType={} path={}",
                originalFilename, contentType, filePath);

        try (InputStream in = Files.newInputStream(filePath)) {
            String raw = tika.parseToString(in);
            String text = raw == null ? "" : raw.trim();
            long durationMs = System.currentTimeMillis() - started;

            if (text.isEmpty()) {
                log.info("Extraction finished status=EMPTY filename={} durationMs={} chars=0",
                        originalFilename, durationMs);
                return new ExtractionResult("", ExtractionStatus.EMPTY, null, false);
            }

            boolean truncated = false;
            if (text.length() > maxExtractedChars) {
                text = text.substring(0, maxExtractedChars);
                truncated = true;
                log.warn("Extracted text truncated filename={} maxChars={}",
                        originalFilename, maxExtractedChars);
            }

            log.info("Extraction finished status=EXTRACTED filename={} durationMs={} chars={} truncated={}",
                    originalFilename, durationMs, text.length(), truncated);
            return new ExtractionResult(text, ExtractionStatus.EXTRACTED, null, truncated);
        } catch (TikaException | IOException e) {
            long durationMs = System.currentTimeMillis() - started;
            log.warn("Extraction failed status=FAILED filename={} durationMs={} reason={}",
                    originalFilename, durationMs, e.getMessage());
            return new ExtractionResult("", ExtractionStatus.FAILED, e.getMessage(), false);
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - started;
            log.warn("Extraction failed status=FAILED filename={} durationMs={} reason={}",
                    originalFilename, durationMs, e.getMessage());
            return new ExtractionResult(
                    "",
                    ExtractionStatus.FAILED,
                    "Unexpected extraction error: " + e.getMessage(),
                    false
            );
        }
    }

    public record ExtractionResult(
            String text,
            ExtractionStatus status,
            String errorMessage,
            boolean truncated
    ) {
    }
}
