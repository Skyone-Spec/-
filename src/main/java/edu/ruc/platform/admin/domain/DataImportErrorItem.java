package edu.ruc.platform.admin.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "data_import_error_item")
public class DataImportErrorItem extends BaseEntity {

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Integer rowNumber;

    @Column(nullable = false, length = 64)
    private String fieldName;

    @Column(nullable = false, length = 500)
    private String errorMessage;

    @Column(length = 500)
    private String rawValue;
}
