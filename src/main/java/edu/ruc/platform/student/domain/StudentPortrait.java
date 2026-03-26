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
@Table(name = "student_portrait")
public class StudentPortrait extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long studentId;

    @Column(length = 16)
    private String gender;

    @Column(length = 32)
    private String ethnicity;

    @Column(length = 255)
    private String honors;

    @Column(length = 255)
    private String scholarships;

    @Column(length = 255)
    private String competitions;

    @Column(length = 255)
    private String socialPractice;

    @Column(length = 255)
    private String volunteerService;

    @Column(length = 255)
    private String researchExperience;

    @Column(length = 255)
    private String disciplineRecords;

    @Column(length = 255)
    private String dailyPerformance;

    @Column
    private Double gpa;

    @Column
    private Integer gradeRank;

    @Column
    private Integer majorRank;

    @Column
    private Integer creditsEarned;

    @Column(length = 128)
    private String careerOrientation;

    @Column(length = 255)
    private String remarks;

    @Column(length = 64)
    private String updatedBy;

    @Column(length = 64)
    private String dataSource;

    @Column(nullable = false)
    private Boolean publicVisible = Boolean.FALSE;
}
