package edu.ruc.platform.student.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "student_volunteer_service_record")
public class StudentVolunteerServiceRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false, length = 255)
    private String serviceProject;

    @Column(length = 255)
    private String serviceLocation;

    @Column(precision = 8, scale = 2)
    private BigDecimal serviceDurationHours;

    @Column(length = 255)
    private String serviceOrganizationName;
}
