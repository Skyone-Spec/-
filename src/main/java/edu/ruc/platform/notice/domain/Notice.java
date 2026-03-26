package edu.ruc.platform.notice.domain;

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
@Table(name = "notice")
public class Notice extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(length = 100)
    private String tag;

    @Column(length = 64)
    private String targetGrade;

    @Column(length = 64)
    private String targetMajor;

    @Column(nullable = false)
    private Boolean targetGraduateOnly = Boolean.FALSE;

    @Column(nullable = false)
    private LocalDateTime publishTime;
}
