package edu.ruc.platform.admin.domain;

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
@Table(name = "audit_import_job")
public class LatestAuditImportJob extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String jobType;

    @Column(nullable = false)
    private Long sourceFileId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private Integer totalRows;

    @Column
    private Integer successRows;

    @Column
    private Integer failedRows;

    @Column
    private String errorMessage;

    @Column
    private String resultJson;

    @Column
    private Long startedBy;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;
}
