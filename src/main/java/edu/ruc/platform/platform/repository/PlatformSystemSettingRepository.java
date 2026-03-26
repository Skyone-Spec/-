package edu.ruc.platform.platform.repository;

import edu.ruc.platform.platform.domain.PlatformSystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformSystemSettingRepository extends JpaRepository<PlatformSystemSetting, Long> {

    Optional<PlatformSystemSetting> findBySettingKey(String settingKey);
}
