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
@Table(name = "aca_program_module")
public class LatestAcademicProgramModule extends BaseEntity {

    @Column(nullable = false)
    private Long programId;

    @Column(nullable = false, length = 64)
    private String moduleCode;

    @Column(nullable = false, length = 128)
    private String moduleName;

    @Column(nullable = false, length = 32)
    private String moduleType;

    @Column(nullable = false)
    private BigDecimal requiredCredits;

    @Column(nullable = false)
    private Integer isDeleted;
}
