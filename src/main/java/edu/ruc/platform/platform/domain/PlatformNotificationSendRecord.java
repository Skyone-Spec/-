package edu.ruc.platform.platform.domain;

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
@Table(name = "platform_notification_send_record")
public class PlatformNotificationSendRecord extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false, length = 32)
    private String targetType;

    @Column(nullable = false, length = 255)
    private String targetDescription;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private Integer recipientCount;

    @Column(nullable = false, length = 64)
    private String triggeredBy;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(length = 255)
    private String extensionChannels;
}
