package edu.ruc.platform.worklog.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "student_work_log")
public class StudentWorkLog extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 64)
    private String studentName;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer workloadScore;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, length = 64)
    private String recorderName;

    @Column(nullable = false, length = 32)
    private String recorderRole;
}
