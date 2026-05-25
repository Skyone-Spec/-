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
@Table(name = "academic_program_module")
public class AcademicProgramModule extends BaseEntity {

    @Column(nullable = false)
    private Long programId;

    @Column(nullable = false, length = 100)
    private String moduleCode;

    @Column(nullable = false, length = 200)
    private String moduleName;

    @Column(nullable = false, length = 50)
    private String moduleType;

    @Column(nullable = false)
    private Integer requiredCredits;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
