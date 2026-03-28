package edu.ruc.platform.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_user_role")
public class LatestUserRole {

    @Id
    private Long id;
}
