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
@Table(name = "advisor_scope_binding")
public class AdvisorScopeBinding extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String advisorUsername;

    @Column(length = 64)
    private String advisorName;

    @Column(length = 32)
    private String grade;

    @Column(length = 32)
    private String className;

    @Column(nullable = false)
    private Long studentId;
}
