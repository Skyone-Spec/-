package edu.ruc.platform.auth.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("mock")
public class MockTokenBlocklistService implements TokenBlocklistService {

    private final Set<String> revoked = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isRevoked(String token) {
        return revoked.contains(hash(token));
    }

    @Override
    public void revoke(String token) {
        revoked.add(hash(token));
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
