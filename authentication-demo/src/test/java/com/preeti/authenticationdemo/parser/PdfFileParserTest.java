package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.dto.ExtractedContentResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class PdfFileParserTest {

    private final PdfFileParser pdfFileParser = new PdfFileParser();

    @Test
    void testSupports() {
        assertTrue(pdfFileParser.supports("pdf", "application/pdf"));
        assertFalse(pdfFileParser.supports("xlsx", "application/vnd.ms-excel"));
    }

    @Test
    void testParse_ValidPdf() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Enterprise File Extraction Test");
                contentStream.endText();
            }
            document.save(out);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ExtractedContentResponse response = pdfFileParser.parse(in);

        assertNotNull(response);
        assertEquals("pdf", response.getFileType());
        assertEquals(1, response.getTotalCount());
        assertTrue(response.getRawText().contains("Enterprise File Extraction Test"));
    }
}
