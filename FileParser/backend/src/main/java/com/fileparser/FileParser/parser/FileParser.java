package com.fileparser.FileParser.parser;

import com.fileparser.FileParser.dto.ParseResult;
import com.fileparser.FileParser.enums.FileType;

import java.io.InputStream;

public interface FileParser {

    FileType supportedType();

    ParseResult parse(InputStream inputStream) throws Exception;
}
