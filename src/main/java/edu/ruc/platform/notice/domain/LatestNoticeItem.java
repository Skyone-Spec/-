package edu.ruc.platform.notice.domain;

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
@Table(name = "notice_item")
public class LatestNoticeItem extends BaseEntity {

    @Column(nullable = false, length = 256)
    private String title;

    @Column
    private String content;

    @Column(length = 128)
    private String sourceName;

    @Column(length = 512)
    private String sourceUrl;

    @Column
    private Long attachmentFileId;

    @Column
    private LocalDateTime publishAt;

    @Column
    private Long createdBy;

    @Column
    private String extJson;

    @Column(nullable = false)
    private Integer isDeleted;
}
