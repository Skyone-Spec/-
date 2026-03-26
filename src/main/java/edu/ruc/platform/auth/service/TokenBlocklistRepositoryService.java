package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.domain.RevokedTokenRecord;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.repository.RevokedTokenRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class TokenBlocklistRepositoryService implements TokenBlocklistService {

    private final RevokedTokenRecordRepository revokedTokenRecordRepository;
    private final TokenService tokenService;

    @Override
    public boolean isRevoked(String token) {
        return revokedTokenRecordRepository.findByTokenHash(hash(token)).isPresent();
    }

    @Override
    public void revoke(String token) {
        String tokenHash = hash(token);
        if (revokedTokenRecordRepository.findByTokenHash(tokenHash).isPresent()) {
            return;
        }
        AuthenticatedUser user = tokenService.parseToken(token);
        RevokedTokenRecord record = new RevokedTokenRecord();
        record.setTokenHash(tokenHash);
        record.setUserId(user.userId());
        record.setUsername(user.username());
        record.setRevokedAt(LocalDateTime.now());
        revokedTokenRecordRepository.save(record);
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
