package com.preeti.authenticationdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "extracted_records")
public class ExtractedRecord {

    @Id
    private String id;

    @Indexed
    private String fileId;

    @Indexed
    private String uploadedBy;

    private int rowIndex;

    @Builder.Default
    private Map<String, String> cellData = new LinkedHashMap<>();

    private String fullRowText;
}
