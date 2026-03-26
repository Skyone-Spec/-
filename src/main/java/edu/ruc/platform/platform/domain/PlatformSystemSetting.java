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
@Table(name = "platform_system_setting")
public class PlatformSystemSetting extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String settingKey;

    @Column(nullable = false, length = 500)
    private String settingValue;

    @Column(length = 255)
    private String description;
}
