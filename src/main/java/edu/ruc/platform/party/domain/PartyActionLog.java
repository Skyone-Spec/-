package edu.ruc.platform.party.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "party_action_log")
public class PartyActionLog extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String flowType;

    @Column(nullable = false, length = 100)
    private String stageName;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 100)
    private String targetType;

    @Column
    private Long targetId;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 50)
    private String operatorName;

    @Column(nullable = false, length = 50)
    private String operatorRole;

    @Column(length = 2000)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime operatedAt;
}
