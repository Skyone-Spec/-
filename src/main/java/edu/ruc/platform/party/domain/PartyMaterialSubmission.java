package edu.ruc.platform.party.domain;

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
@Table(name = "party_material_submission")
public class PartyMaterialSubmission extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String flowType;

    @Column(nullable = false, length = 100)
    private String stageName;

    @Column(nullable = false, length = 100)
    private String materialType;

    @Column(length = 500)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(length = 500)
    private String attachmentPath;

    @Column(nullable = false, length = 32)
    private String status = "PENDING";

    @Column(length = 1000)
    private String reviewComment;

    @Column
    private Long reviewerId;

    @Column(length = 50)
    private String reviewerName;

    @Column
    private LocalDateTime reviewedAt;

    @Column
    private LocalDateTime withdrawalDeadline;
}
