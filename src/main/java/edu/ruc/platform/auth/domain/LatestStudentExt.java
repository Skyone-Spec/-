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
@Table(name = "sys_student_ext")
public class LatestStudentExt {

    @Id
    private String studentNo;

    @Column(length = 128)
    private String majorName;

    @Column
    private Integer gradeYear;

    @Column(length = 128)
    private String className;

    @Column(length = 64)
    private String politicalStatus;

    @Column(length = 64)
    private String partyStatus;

    @Column(length = 64)
    private String idCardHash;

    @Column(length = 64)
    private String homeAddressHash;

    @Column(length = 64)
    private String phoneHash;

    @Column
    private Double gpa;

    @Column
    private String extJson;

    @Column(nullable = false)
    private Integer isDeleted;
}
