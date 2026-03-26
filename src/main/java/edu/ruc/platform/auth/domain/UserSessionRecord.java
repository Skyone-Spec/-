package edu.ruc.platform.auth.domain;

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
@Table(name = "user_session_record")
public class UserSessionRecord extends BaseEntity {

    @Column(nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false)
    private LocalDateTime loginAt;

    @Column
    private LocalDateTime logoutAt;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;
}
