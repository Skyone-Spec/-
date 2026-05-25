package edu.ruc.platform.honor.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "honor_recipient_member")
public class HonorRecipientMember extends BaseEntity {

    @Column(nullable = false)
    private Long recipientId;

    private Long studentId;

    @Column(length = 32)
    private String studentNo;

    @Column(nullable = false, length = 64)
    private String studentName;

    @Column(length = 64)
    private String major;

    @Column(length = 32)
    private String grade;

    @Column(length = 32)
    private String className;

    @Column(length = 64)
    private String memberRole;

    @Column(nullable = false)
    private Integer displayOrder = 0;
}
