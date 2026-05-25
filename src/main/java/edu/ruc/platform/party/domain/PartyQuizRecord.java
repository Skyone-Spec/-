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
@Table(name = "party_quiz_record")
public class PartyQuizRecord extends BaseEntity {

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long bankId;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer correctCount;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false)
    private Boolean passed;

    @Column
    private LocalDateTime completedAt;
}
