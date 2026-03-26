package edu.ruc.platform.admin.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "knowledge_attachment")
public class KnowledgeAttachment extends BaseEntity {

    @Column(nullable = false)
    private Long knowledgeId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false, length = 64)
    private String uploadedBy;
}
