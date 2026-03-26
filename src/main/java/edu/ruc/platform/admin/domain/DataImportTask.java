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
@Table(name = "data_import_task")
public class DataImportTask extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String taskType;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Integer totalRows = 0;

    @Column(nullable = false)
    private Integer successRows = 0;

    @Column(nullable = false)
    private Integer failedRows = 0;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 64)
    private String ownerName;

    @Column(length = 1000)
    private String errorSummary;
}
