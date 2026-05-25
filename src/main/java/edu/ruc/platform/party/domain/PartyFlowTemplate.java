package edu.ruc.platform.party.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "party_flow_template")
public class PartyFlowTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String flowCode;

    @Column(nullable = false, length = 200)
    private String flowName;

    @Column(nullable = false, length = 50)
    private String flowType;

    @Column(nullable = false)
    private Integer totalStages;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
