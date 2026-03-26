package edu.ruc.platform.certificate.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "approval_action_log")
public class ApprovalActionLog extends BaseEntity {

    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 64)
    private String operatorName;

    @Column(nullable = false, length = 32)
    private String operatorRole;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(nullable = false, length = 32)
    private String fromStatus;

    @Column(nullable = false, length = 32)
    private String toStatus;

    @Column(length = 500)
    private String comment;
}
