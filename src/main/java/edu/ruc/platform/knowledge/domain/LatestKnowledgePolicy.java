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
@Table(name = "kb_policy")
public class LatestKnowledgePolicy extends BaseEntity {

    @Column(nullable = false, length = 256)
    private String title;

    @Column
    private String summary;

    @Column
    private String content;

    @Column(length = 32)
    private String sourceType;

    @Column(length = 512)
    private String sourceUrl;

    @Column
    private Long attachmentFileId;

    @Column(nullable = false)
    private Integer isPublished;

    @Column
    private LocalDateTime publishedAt;

    @Column
    private Long createdBy;

    @Column
    private String extJson;

    @Column(nullable = false)
    private Integer isDeleted;
}
