package edu.ruc.platform.party.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "party_flow_node")
public class LatestPartyFlowNode {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long flowId;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false, length = 64)
    private String nodeCode;

    @Column(nullable = false, length = 128)
    private String nodeName;

    @Column
    private String description;

    @Column
    private Integer expectedDays;

    @Column
    private Integer reminderOffsetDays;

    @Column(nullable = false)
    private Integer isDeleted;
}
