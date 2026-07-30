package com.fileparser.FileParser.parser;

import com.fileparser.FileParser.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParserFactory {

    private final List<FileParser> parsers;

    public FileParser getParser(FileType fileType) {

        return parsers.stream()
                .filter(parser -> parser.supportedType() == fileType)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No parser found for " + fileType
                        )
                );
    }
}
