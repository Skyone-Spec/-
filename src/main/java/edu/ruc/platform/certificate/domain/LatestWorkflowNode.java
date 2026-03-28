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
@Table(name = "wf_node")
public class LatestWorkflowNode {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long wfId;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false, length = 128)
    private String nodeName;

    @Column
    private Long approverUserId;

    @Column(nullable = false)
    private Integer isDeleted;
}
