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
@Table(name = "notice_delivery")
public class LatestNoticeDelivery extends BaseEntity {

    @Column(nullable = false)
    private Long noticeId;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column
    private String targetRuleJson;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime sentAt;

    @Column
    private Long createdBy;

    @Column
    private String extJson;
}
