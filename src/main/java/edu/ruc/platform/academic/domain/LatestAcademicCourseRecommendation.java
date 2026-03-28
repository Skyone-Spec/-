package edu.ruc.platform.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "aca_course_recommendation")
public class LatestAcademicCourseRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reportId;

    @Column(nullable = false)
    private Long moduleId;

    @Column(nullable = false)
    private Long courseId;

    @Column
    private String recommendationReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
