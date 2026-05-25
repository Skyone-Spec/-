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
@Table(name = "honor_recipient_attachment")
public class HonorRecipientAttachment extends BaseEntity {

    @Column(nullable = false)
    private Long recipientId;

    private Long fileId;

    @Column(nullable = false, length = 32)
    private String attachmentType;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    private Long fileSize;

    @Column(length = 500)
    private String storagePath;

    @Column(length = 255)
    private String caption;

    @Column(nullable = false)
    private Boolean publicVisible = Boolean.TRUE;

    @Column(nullable = false)
    private Integer displayOrder = 0;
}
