package com.fileparser.FileParser.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedRow {

    private Integer rowNumber;

    private Map<String, Object> values;
}
