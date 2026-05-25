package edu.ruc.platform.academic.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "academic_transcript_item")
public class AcademicTranscriptItem extends BaseEntity {

    @Column(nullable = false)
    private Long transcriptId;

    @Column(nullable = false, length = 20)
    private String term;

    @Column(nullable = false, length = 50)
    private String courseCode;

    @Column(nullable = false, length = 200)
    private String courseName;

    @Column(nullable = false)
    private Double credits;

    @Column(length = 20)
    private String gradeText;

    @Column
    private Double gradePoint;

    @Column(nullable = false)
    private Boolean passed;
}
