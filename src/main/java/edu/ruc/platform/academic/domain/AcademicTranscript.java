package edu.ruc.platform.academic.domain;

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
@Table(name = "academic_transcript")
public class AcademicTranscript extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 20)
    private String term;

    @Column(nullable = false)
    private Double gpa;

    @Column(nullable = false)
    private Double totalCredits;

    @Column(nullable = false)
    private Integer totalCourses;

    @Column(nullable = false)
    private Integer passedCourses;

    @Column(length = 50)
    private String parseStatus = "PARSED";

    @Column
    private LocalDateTime parsedAt;
}
