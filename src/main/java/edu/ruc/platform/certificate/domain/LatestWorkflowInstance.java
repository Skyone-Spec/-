package edu.ruc.platform.certificate.domain;

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
@Table(name = "wf_instance")
public class LatestWorkflowInstance extends BaseEntity {

    @Column(nullable = false)
    private Long wfId;

    @Column(nullable = false, length = 64)
    private String businessTable;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Long startedBy;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;
}
