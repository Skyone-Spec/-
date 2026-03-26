package edu.ruc.platform.worklog.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "student_work_log_action_log")
public class StudentWorkLogActionLog extends BaseEntity {

    @Column(nullable = false)
    private Long workLogId;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 64)
    private String operatorName;

    @Column(nullable = false, length = 32)
    private String operatorRole;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 1000)
    private String detail;
}
