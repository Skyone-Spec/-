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
@Table(name = "academic_warning_record")
public class AcademicWarningRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String moduleName;

    @Column(nullable = false)
    private Integer requiredCredits;

    @Column(nullable = false)
    private Integer earnedCredits;

    @Column(length = 500)
    private String recommendedCourses;
}
