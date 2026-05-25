package edu.ruc.platform.student.domain;

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
@Table(name = "student_student_work_record")
public class StudentStudentWorkRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(nullable = false, length = 255)
    private String organizationName;

    @Column(nullable = false, length = 128)
    private String positionName;

    @Column(length = 1000)
    private String workDescription;
}
