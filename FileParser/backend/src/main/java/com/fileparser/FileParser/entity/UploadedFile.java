package com.fileparser.FileParser.entity;

import com.fileparser.FileParser.enums.FileType;
import com.fileparser.FileParser.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "uploaded_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus status;

    @Column(nullable = false)
    private Integer rowCount;

    @Column(nullable = false)
    private Integer columnCount;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @OneToMany(
            mappedBy = "uploadedFile",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ParsedRecord> records = new ArrayList<>();

    @PrePersist
    void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
