package edu.ruc.platform.certificate.domain;

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
@Table(name = "affair_request")
public class LatestAffairRequest extends BaseEntity {

    @Column(nullable = false)
    private Long requesterUserId;

    @Column(nullable = false, length = 32)
    private String requestType;

    @Column(length = 256)
    private String title;

    @Column(nullable = false, length = 256)
    private String purpose;

    @Column
    private String remark;

    @Column
    private String payloadJson;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime submittedAt;

    @Column
    private LocalDateTime closedAt;

    @Column(nullable = false)
    private Integer isDeleted = 0;
}
