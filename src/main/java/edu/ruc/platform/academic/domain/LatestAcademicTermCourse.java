package edu.ruc.platform.academic.domain;

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
@Table(name = "aca_term_course")
public class LatestAcademicTermCourse extends BaseEntity {

    @Column(nullable = false)
    private Long termId;

    @Column
    private Long courseId;

    @Column(length = 64)
    private String teachingClassCode;

    @Column(nullable = false, length = 64)
    private String courseCode;

    @Column(nullable = false, length = 256)
    private String courseName;

    @Column(length = 128)
    private String teacherName;

    @Column(length = 256)
    private String courseLocation;

    @Column(nullable = false)
    private BigDecimal credits;

    @Column
    private Integer totalHours;

    @Column(nullable = false)
    private Integer isDeleted;
}
