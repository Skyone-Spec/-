package edu.ruc.platform.knowledge.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "file_object")
public class LatestFileObject extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String purpose;

    @Column(nullable = false, length = 256)
    private String originalName;

    @Column(length = 128)
    private String mimeType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(length = 64)
    private String sha256;

    @Column(nullable = false, length = 64)
    private String storageProvider;

    @Column(nullable = false, length = 512)
    private String storagePath;

    @Column
    private Long uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private Integer isDeleted;
}
