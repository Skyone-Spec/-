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
@Table(name = "student_competition_record")
public class StudentCompetitionRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate awardDate;

    @Column(nullable = false, length = 255)
    private String competitionName;

    @Column(length = 64)
    private String competitionLevel;

    @Column(length = 64)
    private String competitionGrade;

    @Column(length = 64)
    private String competitionCategory;

    @Column(length = 255)
    private String organizer;

    @Column(length = 255)
    private String advisorTeacherInfo;

    @Column(length = 1000)
    private String remarks;
}
