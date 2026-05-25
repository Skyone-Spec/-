package edu.ruc.platform.honor.domain;

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
@Table(name = "honor_recipient")
public class HonorRecipient extends BaseEntity {

    @Column(nullable = false)
    private Long showcaseId;

    @Column(nullable = false, length = 32)
    private String recipientType;

    private Long studentId;

    @Column(length = 32)
    private String studentNo;

    @Column(nullable = false, length = 128)
    private String recipientName;

    @Column(length = 64)
    private String major;

    @Column(length = 32)
    private String grade;

    @Column(length = 32)
    private String className;

    @Column(columnDefinition = "TEXT")
    private String awardIntro;

    @Column(columnDefinition = "TEXT")
    private String advancedDeeds;

    private Long photoFileId;

    @Column(nullable = false)
    private Boolean publicVisible = Boolean.FALSE;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    private LocalDateTime displayStartAt;

    private LocalDateTime displayEndAt;

    private Long importTaskId;

    private Long createdById;

    @Column(length = 64)
    private String createdByName;

    @Column(length = 64)
    private String updatedBy;
}
