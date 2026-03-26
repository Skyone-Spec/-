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
@Table(name = "certificate_request")
public class CertificateRequest extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String certificateType;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 500)
    private String reason;

    @Column(length = 255)
    private String generatedPdfPath;

    @Column(nullable = false, length = 32)
    private String currentApproverRole = "COUNSELOR";

    @Column(nullable = false)
    private Integer approvalLevel = 1;

    @Column
    private LocalDateTime withdrawalDeadline;
}
