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
@Table(name = "notice_delivery_target")
public class LatestNoticeDeliveryTarget extends BaseEntity {

    @Column(nullable = false)
    private Long deliveryId;

    @Column(nullable = false)
    private Long targetUserId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime sentAt;

    @Column
    private String errorMessage;
}
