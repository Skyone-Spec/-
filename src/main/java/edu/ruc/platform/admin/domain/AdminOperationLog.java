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
@Table(name = "admin_operation_log")
public class AdminOperationLog extends BaseEntity {

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 64)
    private String operatorName;

    @Column(nullable = false, length = 32)
    private String operatorRole;

    @Column(nullable = false, length = 64)
    private String module;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 255)
    private String target;

    @Column(nullable = false, length = 32)
    private String result;

    @Column(length = 1000)
    private String detail;
}
