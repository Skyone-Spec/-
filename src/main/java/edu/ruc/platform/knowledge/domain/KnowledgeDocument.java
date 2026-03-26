package edu.ruc.platform.knowledge.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private String officialUrl;

    @Column(nullable = false)
    private Boolean published = Boolean.TRUE;

    @Column(length = 255)
    private String sourceFileName;

    @Column(length = 64)
    private String audienceScope;

    @Column(length = 64)
    private String updatedBy;
}
