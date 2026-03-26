package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.ChangePasswordRequest;
import edu.ruc.platform.auth.dto.ChangePasswordResponse;
import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.domain.LoginAuditLog;
import edu.ruc.platform.auth.dto.LoginRequest;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.dto.LogoutResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.auth.dto.WechatLoginRequest;
import edu.ruc.platform.auth.repository.LoginAuditLogRepository;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.service.PlatformSecurityPolicyService;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class AuthService implements AuthApplicationService {

    private final UserAccountRepository userAccountRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUserService currentUserService;
    private final LoginAuditLogRepository loginAuditLogRepository;
    private final TokenBlocklistService tokenBlocklistService;
    private final UserSessionService userSessionService;
    private final PlatformSecurityPolicyService platformSecurityPolicyService;

    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            writeLoginAudit(user.getId(), user.getUsername(), user.getRole().name(), "LOGIN", "DENIED", "账号已禁用");
            throw new BusinessException("账号已禁用");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            writeLoginAudit(user.getId(), user.getUsername(), user.getRole().name(), "LOGIN", "LOCKED", "账号已锁定");
            throw new BusinessException("账号已锁定，请稍后再试");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int failedAttempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= platformSecurityPolicyService.maxFailedLoginAttempts()) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(platformSecurityPolicyService.lockDurationMinutes()));
            }
            userAccountRepository.save(user);
            writeLoginAudit(user.getId(), user.getUsername(), user.getRole().name(), "LOGIN", "FAILED", "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);

        StudentProfile studentProfile = studentProfileRepository.findByStudentNo(user.getUsername()).orElse(null);
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(user, studentProfile);
        String token = tokenService.generateToken(authenticatedUser);
        userSessionService.onLogin(token, authenticatedUser);
        writeLoginAudit(user.getId(), user.getUsername(), user.getRole().name(), "LOGIN", "SUCCESS", null);
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), token, Boolean.TRUE.equals(user.getPasswordResetRequired()));
    }

    @Override
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        throw new BusinessException("暂未接入真实微信登录");
    }

    @Override
    public UserProfileResponse currentUser() {
        return currentUserService.requireCurrentUser().toProfileResponse();
    }

    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        UserAccount user = userAccountRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前用户不存在"));
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetRequired(Boolean.FALSE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user = userAccountRepository.save(user);
        return new ChangePasswordResponse(user.getId(), user.getUsername(), Boolean.TRUE.equals(user.getPasswordResetRequired()), LocalDateTime.now());
    }

    @Override
    public LogoutResponse logout(String token) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        if (token != null && !token.isBlank()) {
            tokenBlocklistService.revoke(token);
            userSessionService.onLogout(token);
        }
        writeLoginAudit(currentUser.userId(), currentUser.username(), currentUser.role(), "LOGOUT", "SUCCESS", null);
        return new LogoutResponse(currentUser.userId(), currentUser.username(), LocalDateTime.now());
    }

    private AuthenticatedUser buildAuthenticatedUser(UserAccount user, StudentProfile studentProfile) {
        if (studentProfile == null) {
            return new AuthenticatedUser(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    null,
                    user.getUsername(),
                    null,
                    null
            );
        }
        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                studentProfile.getStudentNo(),
                studentProfile.getName(),
                studentProfile.getMajor(),
                studentProfile.getGrade()
        );
    }

    private void writeLoginAudit(Long userId, String username, String role, String action, String result, String detail) {
        LoginAuditLog log = new LoginAuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setRole(role);
        log.setAction(action);
        log.setResult(result);
        log.setDetail(detail);
        loginAuditLogRepository.save(log);
    }
}
