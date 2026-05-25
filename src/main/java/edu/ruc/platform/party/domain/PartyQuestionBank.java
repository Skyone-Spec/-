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
@Table(name = "party_question_bank")
public class PartyQuestionBank extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String bankCode;

    @Column(nullable = false, length = 200)
    private String bankName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer questionCount;

    @Column(nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(length = 500)
    private String description;
}
