package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyUpdateRequest;
import edu.ruc.platform.platform.domain.PlatformSystemSetting;
import edu.ruc.platform.platform.repository.PlatformSystemSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!mock")
public class DefaultPlatformSecurityPolicyService implements PlatformSecurityPolicyService {

    private static final String KEY_MAX_FAILED_LOGIN_ATTEMPTS = "platform.security.max-failed-login-attempts";
    private static final String KEY_LOCK_DURATION_MINUTES = "platform.security.lock-duration-minutes";
    private static final String KEY_DEFAULT_PASSWORD = "platform.security.default-password";
    private static final String KEY_REQUIRE_PASSWORD_RESET_ON_CREATE = "platform.security.require-password-reset-on-create";

    private final PlatformSystemSettingRepository platformSystemSettingRepository;
    private final int defaultMaxFailedLoginAttempts;
    private final int defaultLockDurationMinutes;
    private final String configuredDefaultPassword;
    private final boolean configuredRequirePasswordResetOnCreate;

    public DefaultPlatformSecurityPolicyService(
            PlatformSystemSettingRepository platformSystemSettingRepository,
            @Value("${platform.security.max-failed-login-attempts:5}") int maxFailedLoginAttempts,
            @Value("${platform.security.lock-duration-minutes:30}") int lockDurationMinutes,
            @Value("${platform.security.default-password:123456}") String defaultPassword,
            @Value("${platform.security.require-password-reset-on-create:true}") boolean requirePasswordResetOnCreate) {
        this.platformSystemSettingRepository = platformSystemSettingRepository;
        this.defaultMaxFailedLoginAttempts = maxFailedLoginAttempts;
        this.defaultLockDurationMinutes = lockDurationMinutes;
        this.configuredDefaultPassword = defaultPassword;
        this.configuredRequirePasswordResetOnCreate = requirePasswordResetOnCreate;
    }

    @Override
    public synchronized PlatformSecurityPolicyResponse getPolicy() {
        return new PlatformSecurityPolicyResponse(
                maxFailedLoginAttempts(),
                lockDurationMinutes(),
                defaultPassword(),
                requirePasswordResetOnCreate()
        );
    }

    @Override
    public synchronized PlatformSecurityPolicyResponse updatePolicy(PlatformSecurityPolicyUpdateRequest request) {
        if (request.maxFailedLoginAttempts() != null) {
            if (request.maxFailedLoginAttempts() <= 0) {
                throw new BusinessException("最大失败登录次数必须大于 0");
            }
            saveSetting(KEY_MAX_FAILED_LOGIN_ATTEMPTS, String.valueOf(request.maxFailedLoginAttempts()), "最大失败登录次数");
        }
        if (request.lockDurationMinutes() != null) {
            if (request.lockDurationMinutes() <= 0) {
                throw new BusinessException("锁定时长必须大于 0 分钟");
            }
            saveSetting(KEY_LOCK_DURATION_MINUTES, String.valueOf(request.lockDurationMinutes()), "账号锁定时长（分钟）");
        }
        if (request.defaultPassword() != null) {
            if (request.defaultPassword().isBlank()) {
                throw new BusinessException("默认密码不能为空");
            }
            saveSetting(KEY_DEFAULT_PASSWORD, request.defaultPassword(), "平台默认密码");
        }
        if (request.requirePasswordResetOnCreate() != null) {
            saveSetting(KEY_REQUIRE_PASSWORD_RESET_ON_CREATE, String.valueOf(request.requirePasswordResetOnCreate()), "创建账号后是否强制改密");
        }
        return getPolicy();
    }

    @Override
    public synchronized int maxFailedLoginAttempts() {
        return getIntSetting(KEY_MAX_FAILED_LOGIN_ATTEMPTS, defaultMaxFailedLoginAttempts);
    }

    @Override
    public synchronized int lockDurationMinutes() {
        return getIntSetting(KEY_LOCK_DURATION_MINUTES, defaultLockDurationMinutes);
    }

    @Override
    public synchronized String defaultPassword() {
        return getStringSetting(KEY_DEFAULT_PASSWORD, configuredDefaultPassword);
    }

    @Override
    public synchronized boolean requirePasswordResetOnCreate() {
        return getBooleanSetting(KEY_REQUIRE_PASSWORD_RESET_ON_CREATE, configuredRequirePasswordResetOnCreate);
    }

    private int getIntSetting(String key, int defaultValue) {
        String value = platformSystemSettingRepository.findBySettingKey(key)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException("平台配置格式错误: " + key);
        }
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = platformSystemSettingRepository.findBySettingKey(key)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    private String getStringSetting(String key, String defaultValue) {
        return platformSystemSettingRepository.findBySettingKey(key)
                .map(PlatformSystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    private void saveSetting(String key, String value, String description) {
        PlatformSystemSetting setting = platformSystemSettingRepository.findBySettingKey(key)
                .orElseGet(PlatformSystemSetting::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        platformSystemSettingRepository.save(setting);
    }
}
