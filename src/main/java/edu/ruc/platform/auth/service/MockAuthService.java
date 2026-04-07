package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.ChangePasswordRequest;
import edu.ruc.platform.auth.dto.ChangePasswordResponse;
import edu.ruc.platform.auth.dto.LoginRequest;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.LogoutResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.auth.dto.WechatLoginRequest;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.dto.PlatformUserDetailResponse;
import edu.ruc.platform.platform.service.PlatformSecurityPolicyService;
import edu.ruc.platform.platform.service.MockPlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockAuthService implements AuthApplicationService {

    private final TokenService tokenService;
    private final CurrentUserService currentUserService;
    private final TokenBlocklistService tokenBlocklistService;
    private final UserSessionService userSessionService;
    private final PlatformSecurityPolicyService platformSecurityPolicyService;
    private final MockPlatformService mockPlatformService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String defaultPassword = platformSecurityPolicyService.defaultPassword();
        PlatformUserDetailResponse platformUser = mockPlatformService.findUserByUsername(request.username());
        if (platformUser != null) {
            if (!Boolean.TRUE.equals(platformUser.enabled())) {
                throw new BusinessException("账号已被禁用");
            }
            if (platformUser.locked()) {
                throw new BusinessException("账号已被锁定");
            }
        }
        if ("admin".equals(request.username()) && defaultPassword.equals(request.password())) {
            mockPlatformService.clearLoginFailures(request.username());
            return issueToken(new AuthenticatedUser(1L, null, "admin", "SUPER_ADMIN", null, "系统管理员", null, null), platformUser != null && Boolean.TRUE.equals(platformUser.passwordResetRequired()));
        }
        if ("teacher01".equals(request.username()) && defaultPassword.equals(request.password())) {
            mockPlatformService.clearLoginFailures(request.username());
            return issueToken(new AuthenticatedUser(20001L, null, "teacher01", "COUNSELOR", null, "胡浩老师", null, null), platformUser != null && Boolean.TRUE.equals(platformUser.passwordResetRequired()));
        }
        if ("advisor01".equals(request.username()) && defaultPassword.equals(request.password())) {
            mockPlatformService.clearLoginFailures(request.username());
            return issueToken(new AuthenticatedUser(20002L, null, "advisor01", "CLASS_ADVISOR", null, "王老师", null, "2023级"), platformUser != null && Boolean.TRUE.equals(platformUser.passwordResetRequired()));
        }
        if ("2023100002".equals(request.username()) && defaultPassword.equals(request.password())) {
            mockPlatformService.clearLoginFailures(request.username());
            return issueToken(new AuthenticatedUser(10002L, 10002L, "2023100002", "LEAGUE_SECRETARY", "2023100002", "李四", "计算机类", "2023级"), platformUser != null && Boolean.TRUE.equals(platformUser.passwordResetRequired()));
        }
        if ("2023100001".equals(request.username()) && defaultPassword.equals(request.password())) {
            mockPlatformService.clearLoginFailures(request.username());
            return issueToken(new AuthenticatedUser(10001L, 10001L, "2023100001", "STUDENT", "2023100001", "张三", "计算机类", "2023级"), platformUser != null && Boolean.TRUE.equals(platformUser.passwordResetRequired()));
        }
        if (platformUser != null) {
            mockPlatformService.markLoginFailure(request.username(), platformSecurityPolicyService.maxFailedLoginAttempts());
            PlatformUserDetailResponse latest = mockPlatformService.findUserByUsername(request.username());
            if (latest != null && latest.locked()) {
                throw new BusinessException("账号已被锁定");
            }
        }
        throw new BusinessException("用户名或密码错误，演示账号: admin/%s、teacher01/%s、advisor01/%s、2023100001/%s、2023100002/%s"
                .formatted(defaultPassword, defaultPassword, defaultPassword, defaultPassword, defaultPassword));
    }

    @Override
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        if (request.code().isBlank()) {
            throw new BusinessException("微信登录 code 无效");
        }
        return issueToken(new AuthenticatedUser(10001L, 10001L, "2023100001", "STUDENT", "2023100001", "张三", "计算机类", "2023级"), false);
    }

    @Override
    public UserProfileResponse currentUser() {
        return currentUserService.requireCurrentUser().toProfileResponse();
    }

    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        if (!platformSecurityPolicyService.defaultPassword().equals(request.oldPassword())) {
            throw new BusinessException("旧密码错误");
        }
        mockPlatformService.clearPasswordResetRequired(currentUser.username());
        return new ChangePasswordResponse(currentUser.userId(), currentUser.username(), false, LocalDateTime.now());
    }

    @Override
    public LogoutResponse logout(String token) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        if (token != null && !token.isBlank()) {
            tokenBlocklistService.revoke(token);
        }
        return new LogoutResponse(currentUser.userId(), currentUser.username(), LocalDateTime.now());
    }

    private LoginResponse issueToken(AuthenticatedUser user, boolean passwordResetRequired) {
        String token = tokenService.generateToken(user);
        userSessionService.onLogin(token, user);
        return new LoginResponse(user.userId(), user.username(), user.role(), token, passwordResetRequired);
    }
}
