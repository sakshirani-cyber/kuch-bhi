package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.dto.ExtractedContentResponse;

import java.io.InputStream;

public interface FileParser {

    /**
     * Checks if this parser strategy supports the given file extension or MIME content-type.
     */
    boolean supports(String fileExtension, String contentType);

    /**
     * Parses the file input stream and extracts structured content.
     */
    ExtractedContentResponse parse(InputStream inputStream);
}
