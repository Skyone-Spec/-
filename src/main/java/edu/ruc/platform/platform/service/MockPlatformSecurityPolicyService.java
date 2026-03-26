package edu.ruc.platform.platform.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyUpdateRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockPlatformSecurityPolicyService implements PlatformSecurityPolicyService {

    private int maxFailedLoginAttempts = 5;
    private int lockDurationMinutes = 30;
    private String defaultPassword = "123456";
    private boolean requirePasswordResetOnCreate = true;

    @Override
    public synchronized PlatformSecurityPolicyResponse getPolicy() {
        return new PlatformSecurityPolicyResponse(
                maxFailedLoginAttempts,
                lockDurationMinutes,
                defaultPassword,
                requirePasswordResetOnCreate
        );
    }

    @Override
    public synchronized PlatformSecurityPolicyResponse updatePolicy(PlatformSecurityPolicyUpdateRequest request) {
        if (request.maxFailedLoginAttempts() != null) {
            if (request.maxFailedLoginAttempts() <= 0) {
                throw new BusinessException("最大失败登录次数必须大于 0");
            }
            maxFailedLoginAttempts = request.maxFailedLoginAttempts();
        }
        if (request.lockDurationMinutes() != null) {
            if (request.lockDurationMinutes() <= 0) {
                throw new BusinessException("锁定时长必须大于 0 分钟");
            }
            lockDurationMinutes = request.lockDurationMinutes();
        }
        if (request.defaultPassword() != null) {
            if (request.defaultPassword().isBlank()) {
                throw new BusinessException("默认密码不能为空");
            }
            defaultPassword = request.defaultPassword();
        }
        if (request.requirePasswordResetOnCreate() != null) {
            requirePasswordResetOnCreate = request.requirePasswordResetOnCreate();
        }
        return getPolicy();
    }

    @Override
    public synchronized int maxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    @Override
    public synchronized int lockDurationMinutes() {
        return lockDurationMinutes;
    }

    @Override
    public synchronized String defaultPassword() {
        return defaultPassword;
    }

    @Override
    public synchronized boolean requirePasswordResetOnCreate() {
        return requirePasswordResetOnCreate;
    }
}
