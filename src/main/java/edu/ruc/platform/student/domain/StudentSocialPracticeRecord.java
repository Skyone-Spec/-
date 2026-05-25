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
@Table(name = "student_social_practice_record")
public class StudentSocialPracticeRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate practiceStartDate;

    @Column
    private LocalDate practiceEndDate;

    @Column(nullable = false, length = 255)
    private String practiceTeamName;

    @Column(nullable = false, length = 255)
    private String practiceTheme;

    @Column(length = 255)
    private String practiceLocation;

    @Column(length = 64)
    private String practiceTeamLevel;

    @Column(length = 255)
    private String advisorTeacher;
}
