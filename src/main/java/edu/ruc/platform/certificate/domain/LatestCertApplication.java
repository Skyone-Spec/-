package edu.ruc.platform.certificate.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cert_application")
public class LatestCertApplication extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long requestId;

    @Column(nullable = false)
    private Long templateId;

    @Column
    private Long generatedPdfFileId;

    @Column
    private String studentSnapshotJson;
}
