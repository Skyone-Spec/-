package edu.ruc.platform.party.domain;

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
@Table(name = "party_progress_record")
public class PartyProgressRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 64)
    private String currentStage;

    @Column(nullable = false)
    private LocalDate stageStartDate;

    @Column(length = 500)
    private String completedActions;

    @Column(length = 500)
    private String nextAction;
}
