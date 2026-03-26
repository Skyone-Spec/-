package edu.ruc.platform.student.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "student_status_history")
public class StudentStatusHistory extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 64)
    private String fromStatus;

    @Column(nullable = false, length = 64)
    private String toStatus;

    @Column(length = 64)
    private String changedToMajor;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false, length = 64)
    private String changedBy;

    @Column(nullable = false, length = 32)
    private String changedByRole;
}
