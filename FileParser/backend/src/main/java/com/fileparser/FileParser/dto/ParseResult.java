package com.fileparser.FileParser.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParseResult {

    private List<String> headers;

    private List<ParsedRow> rows;
}
