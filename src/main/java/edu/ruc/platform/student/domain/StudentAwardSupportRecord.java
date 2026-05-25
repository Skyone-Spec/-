package edu.ruc.platform.student.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "student_award_support_record")
public class StudentAwardSupportRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 32)
    private String assessmentAcademicYear;

    @Column(nullable = false, length = 128)
    private String awardName;

    @Column(length = 128)
    private String batchName;

    @Column(length = 64)
    private String awardLevel;

    @Column(length = 64)
    private String awardGrade;

    @Column(precision = 12, scale = 2)
    private BigDecimal awardAmount;

    @Column(length = 64)
    private String awardType;
}
