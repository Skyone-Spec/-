package edu.ruc.platform.academic.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "academic_program")
public class AcademicProgram extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String programCode;

    @Column(nullable = false, length = 200)
    private String programName;

    @Column(nullable = false, length = 50)
    private String major;

    @Column(nullable = false, length = 20)
    private String grade;

    @Column(nullable = false)
    private Integer totalCredits;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
