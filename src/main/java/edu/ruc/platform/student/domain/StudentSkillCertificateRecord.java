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
@Table(name = "student_skill_certificate_record")
public class StudentSkillCertificateRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 255)
    private String certificateName;

    @Column(nullable = false)
    private LocalDate obtainedDate;

    @Column(length = 64)
    private String certificateLevel;

    @Column(length = 1000)
    private String description;
}
