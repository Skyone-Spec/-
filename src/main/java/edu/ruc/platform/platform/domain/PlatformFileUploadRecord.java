package edu.ruc.platform.platform.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "platform_file_upload_record")
public class PlatformFileUploadRecord extends BaseEntity {

    @Column(nullable = false, length = 64)
    private String bizType;

    @Column
    private Long bizId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false)
    private Long uploadedById;

    @Column(nullable = false, length = 64)
    private String uploadedBy;

    @Column(nullable = false)
    private Boolean archived = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;
}
