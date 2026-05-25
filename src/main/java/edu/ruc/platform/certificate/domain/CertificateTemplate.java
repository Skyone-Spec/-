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
@Table(name = "certificate_template")
public class CertificateTemplate extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String templateCode;

    @Column(nullable = false, length = 200)
    private String templateName;

    @Column(nullable = false, length = 100)
    private String certificateType;

    @Column(length = 2000)
    private String templateContent;

    @Column(length = 500)
    private String templateFilePath;

    @Column(length = 50)
    private String outputFormat = "PDF";

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(length = 500)
    private String description;

    @Column(length = 64)
    private String updatedBy;
}
