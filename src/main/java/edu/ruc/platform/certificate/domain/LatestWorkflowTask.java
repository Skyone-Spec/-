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
@Table(name = "wf_task")
public class LatestWorkflowTask extends BaseEntity {

    @Column(nullable = false)
    private Long wfInstanceId;

    @Column(nullable = false)
    private Long wfNodeId;

    @Column
    private Long assigneeUserId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime dueAt;

    @Column
    private LocalDateTime completedAt;
}
