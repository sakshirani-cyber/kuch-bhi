package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.dto.ExtractedContentResponse;
import com.preeti.authenticationdemo.exception.FileExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class PdfFileParser implements FileParser {

    @Override
    public boolean supports(String fileExtension, String contentType) {
        if (fileExtension != null && fileExtension.equalsIgnoreCase("pdf")) {
            return true;
        }
        return contentType != null && contentType.equalsIgnoreCase("application/pdf");
    }

    @Override
    public ExtractedContentResponse parse(InputStream inputStream) {
        log.info("PDF extraction started");
        try {
            byte[] bytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String extractedText = stripper.getText(document);

                int pageCount = document.getNumberOfPages();
                log.info("PDF extraction completed cleanly. Extracted {} pages", pageCount);

                return ExtractedContentResponse.builder()
                        .fileType("pdf")
                        .totalCount(pageCount)
                        .rawText(extractedText != null ? extractedText.trim() : "")
                        .build();
            }
        } catch (Exception exception) {
            log.error("Failed to extract content from PDF file", exception);
            throw new FileExtractionException("Could not extract readable text from PDF: " + exception.getMessage(), exception);
        }
    }
}
