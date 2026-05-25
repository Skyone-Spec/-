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
@Table(name = "student_innovation_entrepreneurship_record")
public class StudentInnovationEntrepreneurshipRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 64)
    private String projectCode;

    @Column(nullable = false, length = 255)
    private String projectName;

    @Column(length = 128)
    private String collegeName;

    @Column(length = 64)
    private String projectStatus;

    @Column(length = 64)
    private String projectLevel;

    @Column(length = 64)
    private String completionGrade;

    @Column(length = 64)
    private String participantRole;

    @Column(length = 64)
    private String projectType;

    @Column(length = 64)
    private String projectBatch;

    @Column
    private Integer participantCount;

    @Column(length = 255)
    private String advisorTeacher;
}
