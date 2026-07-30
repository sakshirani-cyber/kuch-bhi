package com.preeti.authenticationdemo.parser;

import com.preeti.authenticationdemo.exception.InvalidFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FileParserFactory {

    private final List<FileParser> parsers;

    public FileParserFactory(List<FileParser> parsers) {
        this.parsers = parsers;
    }

    public FileParser getParser(String fileExtension, String contentType) {
        return parsers.stream()
                .filter(parser -> parser.supports(fileExtension, contentType))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("No parser strategy registered for extension: '{}', contentType: '{}'", fileExtension, contentType);
                    return new InvalidFileTypeException("Unsupported file format or content type: " + fileExtension);
                });
    }
}
