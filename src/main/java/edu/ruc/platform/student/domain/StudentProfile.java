package edu.ruc.platform.student.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "student_profile")
public class StudentProfile extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String studentNo;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 64)
    private String major;

    @Column(length = 32)
    private String grade;

    @Column(length = 32)
    private String className;

    @Column(length = 64)
    private String advisorScope;

    @Column(length = 32)
    private String degreeLevel;

    @Column(length = 128)
    private String encryptedIdCardNo;

    @Column(length = 128)
    private String encryptedPhone;

    @Column(length = 128)
    private String email;

    @Column(nullable = false)
    private Boolean graduated = Boolean.FALSE;

    @Column(length = 64)
    private String status;

    @Column(length = 64)
    private String majorChangedTo;

    @Column(length = 128)
    private String encryptedNativePlace;

    @Column(length = 128)
    private String encryptedHouseholdAddress;

    @Column(length = 128)
    private String encryptedSupervisor;
}
