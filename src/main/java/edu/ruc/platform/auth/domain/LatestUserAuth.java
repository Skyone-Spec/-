package edu.ruc.platform.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_user_auth")
public class LatestUserAuth {

    @Id
    private Long id;

    @Column(nullable = false, length = 32)
    private String studentNo;

    @Column(nullable = false, length = 32)
    private String loginMethod;

    @Column(length = 64)
    private String wechatOpenid;

    @Column(length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private Integer isDeleted;
}
