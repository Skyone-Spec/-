package edu.ruc.platform.academic.domain;

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
@Table(name = "aca_audit_report")
public class LatestAcademicAuditReport extends BaseEntity {

    @Column(nullable = false)
    private Long studentUserId;

    @Column(nullable = false)
    private Long programId;

    @Column(nullable = false)
    private Long transcriptId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Integer missingModuleCount;

    @Column(nullable = false)
    private LocalDateTime generatedAt;
}
