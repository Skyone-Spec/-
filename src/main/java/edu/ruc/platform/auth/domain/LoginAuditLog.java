package edu.ruc.platform.auth.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "login_audit_log")
public class LoginAuditLog extends BaseEntity {

    @Column
    private Long userId;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(length = 32)
    private String role;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(nullable = false, length = 32)
    private String result;

    @Column(length = 255)
    private String detail;
}
