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
@Table(name = "honor_showcase")
public class HonorShowcase extends BaseEntity {

    @Column(nullable = false)
    private Integer awardYear;

    @Column(nullable = false, length = 100)
    private String honorCategory;

    @Column(nullable = false, length = 32)
    private String recipientType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

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
