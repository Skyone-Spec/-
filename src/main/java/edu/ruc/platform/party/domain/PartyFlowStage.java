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
@Table(name = "party_flow_stage")
public class PartyFlowStage extends BaseEntity {

    @Column(nullable = false)
    private Long flowId;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false, length = 100)
    private String stageCode;

    @Column(nullable = false, length = 200)
    private String stageName;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String requiredMaterials;

    @Column
    private Integer estimatedDays;

    @Column
    private Integer reminderDaysBefore;

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;
}
