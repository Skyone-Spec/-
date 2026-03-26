package edu.ruc.platform.auth.domain;

import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_account")
public class UserAccount extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoleType role;

    @Column(nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @Column(length = 128)
    private String wechatOpenId;

    @Column(nullable = false)
    private Boolean passwordResetRequired = Boolean.FALSE;

    @Column(nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column
    private LocalDateTime lockedUntil;
}
