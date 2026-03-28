package edu.ruc.platform.knowledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cert_template")
public class LatestCertTemplate {

    @Id
    private Long id;

    @Column(nullable = false, length = 64)
    private String templateCode;

    @Column(nullable = false, length = 128)
    private String templateName;

    @Column(nullable = false)
    private Long fileId;

    @Column(nullable = false)
    private Integer isActive;

    @Column(nullable = false)
    private Integer isDeleted;
}
