package edu.ruc.platform.academic.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "aca_transcript")
public class LatestAcademicTranscript extends BaseEntity {

    @Column(nullable = false)
    private Long studentUserId;

    @Column(nullable = false)
    private Long sourceFileId;

    @Column(nullable = false, length = 32)
    private String parseStatus;

    @Column
    private LocalDateTime parsedAt;

    @Column
    private BigDecimal totalCredits;

    @Column
    private BigDecimal gpa;
}
