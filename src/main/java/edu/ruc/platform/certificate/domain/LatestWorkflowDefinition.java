package edu.ruc.platform.certificate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "wf_definition")
public class LatestWorkflowDefinition {

    @Id
    private Long id;

    @Column(nullable = false, length = 64)
    private String wfCode;

    @Column(nullable = false, length = 64)
    private String businessType;

    @Column(nullable = false)
    private Integer isActive;

    @Column(nullable = false)
    private Integer isDeleted;
}
