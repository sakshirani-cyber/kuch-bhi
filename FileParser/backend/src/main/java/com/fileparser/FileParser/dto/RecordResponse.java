package com.fileparser.FileParser.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordResponse {

    private Long id;

    private Integer rowNumber;

    private Map<String, Object> data;
}
