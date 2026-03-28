package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.domain.LatestStudentExt;
import edu.ruc.platform.auth.domain.LatestUser;
import edu.ruc.platform.auth.domain.LatestUserAuth;
import edu.ruc.platform.auth.domain.LoginAuditLog;
import edu.ruc.platform.auth.domain.UserAccount;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.dto.ChangePasswordRequest;
import edu.ruc.platform.auth.dto.ChangePasswordResponse;
import edu.ruc.platform.auth.dto.LoginRequest;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.LogoutResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.auth.dto.WechatLoginRequest;
import edu.ruc.platform.auth.repository.LatestStudentExtRepository;
import edu.ruc.platform.auth.repository.LatestUserAuthRepository;
import edu.ruc.platform.auth.repository.LatestUserRepository;
import edu.ruc.platform.auth.repository.LatestUserRoleRepository;
import edu.ruc.platform.auth.repository.LoginAuditLogRepository;
import edu.ruc.platform.auth.repository.UserAccountRepository;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.platform.service.PlatformSecurityPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseAuthService implements AuthApplicationService {

    private final LatestUserRepository latestUserRepository;
    private final LatestUserAuthRepository latestUserAuthRepository;
    private final LatestStudentExtRepository latestStudentExtRepository;
    private final LatestUserRoleRepository latestUserRoleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CurrentUserService currentUserService;
    private final LoginAuditLogRepository loginAuditLogRepository;
    private final TokenBlocklistService tokenBlocklistService;
    private final UserSessionService userSessionService;
    private final PlatformSecurityPolicyService platformSecurityPolicyService;

    @Override
    public LoginResponse login(LoginRequest request) {
        LatestUser latestUser = latestUserRepository.findByStudentNoAndIsDeleted(request.username(), 0)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        LatestUserAuth latestAuth = latestUserAuthRepository
                .findByStudentNoAndLoginMethodAndIsDeleted(latestUser.getStudentNo(), "password", 0)
                .orElseThrow(() -> new BusinessException("当前账号未开通密码登录"));
        UserAccount overlay = ensureOverlayAccount(latestUser, latestAuth);

        if (!Boolean.TRUE.equals(overlay.getEnabled()) || !"active".equalsIgnoreCase(latestUser.getStatus())) {
            writeLoginAudit(latestUser.getId(), latestUser.getStudentNo(), overlay.getRole().name(), "LOGIN", "DENIED", "账号已禁用");
            throw new BusinessException("账号已禁用");
        }
        if (overlay.getLockedUntil() != null && overlay.getLockedUntil().isAfter(LocalDateTime.now())) {
            writeLoginAudit(latestUser.getId(), latestUser.getStudentNo(), overlay.getRole().name(), "LOGIN", "LOCKED", "账号已锁定");
            throw new BusinessException("账号已锁定，请稍后再试");
        }
        if (!passwordEncoder.matches(request.password(), latestAuth.getPasswordHash())) {
            int failedAttempts = (overlay.getFailedLoginAttempts() == null ? 0 : overlay.getFailedLoginAttempts()) + 1;
            overlay.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= platformSecurityPolicyService.maxFailedLoginAttempts()) {
                overlay.setLockedUntil(LocalDateTime.now().plusMinutes(platformSecurityPolicyService.lockDurationMinutes()));
            }
            userAccountRepository.save(overlay);
            writeLoginAudit(latestUser.getId(), latestUser.getStudentNo(), overlay.getRole().name(), "LOGIN", "FAILED", "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        overlay.setFailedLoginAttempts(0);
        overlay.setLockedUntil(null);
        overlay = userAccountRepository.save(overlay);

        LatestStudentExt ext = latestStudentExtRepository.findByStudentNoAndIsDeleted(latestUser.getStudentNo(), 0).orElse(null);
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(latestUser, overlay.getRole(), ext);
        String token = tokenService.generateToken(authenticatedUser);
        userSessionService.onLogin(token, authenticatedUser);
        writeLoginAudit(latestUser.getId(), latestUser.getStudentNo(), overlay.getRole().name(), "LOGIN", "SUCCESS", null);
        return new LoginResponse(
                latestUser.getId(),
                latestUser.getStudentNo(),
                overlay.getRole().name(),
                token,
                Boolean.TRUE.equals(overlay.getPasswordResetRequired())
        );
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
        LatestUser latestUser = latestUserRepository.findById(currentUser.userId())
                .orElseThrow(() -> new BusinessException("当前用户不存在"));
        LatestUserAuth latestAuth = latestUserAuthRepository
                .findByStudentNoAndLoginMethodAndIsDeleted(latestUser.getStudentNo(), "password", 0)
                .orElseThrow(() -> new BusinessException("当前账号未开通密码登录"));
        if (!passwordEncoder.matches(request.oldPassword(), latestAuth.getPasswordHash())) {
            throw new BusinessException("旧密码错误");
        }
        latestAuth.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        latestUserAuthRepository.save(latestAuth);

        UserAccount overlay = ensureOverlayAccount(latestUser, latestAuth);
        overlay.setPasswordHash(latestAuth.getPasswordHash());
        overlay.setPasswordResetRequired(Boolean.FALSE);
        overlay.setFailedLoginAttempts(0);
        overlay.setLockedUntil(null);
        userAccountRepository.save(overlay);
        return new ChangePasswordResponse(latestUser.getId(), latestUser.getStudentNo(), false, LocalDateTime.now());
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

    private UserAccount ensureOverlayAccount(LatestUser latestUser, LatestUserAuth latestAuth) {
        return userAccountRepository.findById(latestUser.getId()).orElseGet(() -> {
            UserAccount created = new UserAccount();
            created.setId(latestUser.getId());
            created.setUsername(latestUser.getStudentNo());
            created.setPasswordHash(latestAuth.getPasswordHash());
            created.setRole(resolveRole(latestUser.getId()));
            created.setEnabled("active".equalsIgnoreCase(latestUser.getStatus()));
            created.setWechatOpenId(null);
            created.setPasswordResetRequired(Boolean.FALSE);
            created.setFailedLoginAttempts(0);
            created.setLockedUntil(null);
            return userAccountRepository.save(created);
        });
    }

    private AuthenticatedUser buildAuthenticatedUser(LatestUser latestUser, RoleType role, LatestStudentExt ext) {
        return new AuthenticatedUser(
                latestUser.getId(),
                latestUser.getStudentNo(),
                role.name(),
                latestUser.getStudentNo(),
                latestUser.getFullName(),
                ext == null ? null : ext.getMajorName(),
                ext == null || ext.getGradeYear() == null ? null : ext.getGradeYear() + "级"
        );
    }

    private RoleType resolveRole(Long userId) {
        List<String> roleCodes = latestUserRoleRepository.findRoleCodesByUserId(userId);
        String roleCode = roleCodes.stream().findFirst().orElse("student");
        return switch (roleCode.toLowerCase(Locale.ROOT)) {
            case "cadre" -> RoleType.LEAGUE_SECRETARY;
            case "teacher_admin" -> RoleType.COUNSELOR;
            case "college_leader" -> RoleType.SUPER_ADMIN;
            default -> RoleType.STUDENT;
        };
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
