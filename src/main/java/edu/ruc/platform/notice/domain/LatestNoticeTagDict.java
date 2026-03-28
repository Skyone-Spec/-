package edu.ruc.platform.notice.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notice_tag_dict")
public class LatestNoticeTagDict extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String tagCode;

    @Column(nullable = false, length = 64)
    private String tagName;

    @Column(nullable = false)
    private Integer isDeleted;
}
