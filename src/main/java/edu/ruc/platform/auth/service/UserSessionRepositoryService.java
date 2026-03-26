package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.domain.UserSessionRecord;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.repository.UserSessionRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class UserSessionRepositoryService implements UserSessionService {

    private final UserSessionRecordRepository userSessionRecordRepository;

    @Override
    public void onLogin(String token, AuthenticatedUser user) {
        UserSessionRecord record = new UserSessionRecord();
        record.setTokenHash(hash(token));
        record.setUserId(user.userId());
        record.setUsername(user.username());
        record.setRole(user.role());
        record.setLoginAt(LocalDateTime.now());
        record.setActive(Boolean.TRUE);
        userSessionRecordRepository.save(record);
    }

    @Override
    public void onLogout(String token) {
        userSessionRecordRepository.findByTokenHash(hash(token)).ifPresent(record -> {
            record.setActive(Boolean.FALSE);
            record.setLogoutAt(LocalDateTime.now());
            userSessionRecordRepository.save(record);
        });
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("token hash failed", ex);
        }
    }
}
