package edu.ruc.platform.party.domain;

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
@Table(name = "party_student_progress")
public class LatestPartyStudentProgress extends BaseEntity {

    @Column(nullable = false)
    private Long studentUserId;

    @Column(nullable = false)
    private Long flowId;

    @Column
    private Long currentNodeId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime updatedNodeAt;

    @Column
    private LocalDateTime nextDeadlineAt;

    @Column(nullable = false)
    private Integer isDeleted;
}
