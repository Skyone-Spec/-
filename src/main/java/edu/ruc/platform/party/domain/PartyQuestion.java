package edu.ruc.platform.party.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "party_question")
public class PartyQuestion extends BaseEntity {

    @Column(nullable = false)
    private Long bankId;

    @Column(nullable = false)
    private Integer seqNo;

    @Column(nullable = false, length = 2000)
    private String questionText;

    @Column(nullable = false, length = 2000)
    private String options;

    @Column(nullable = false, length = 10)
    private String correctAnswer;

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false)
    private Integer score = 1;
}
