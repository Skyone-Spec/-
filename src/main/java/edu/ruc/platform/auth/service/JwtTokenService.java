package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService implements TokenService {

    @Value("${platform.security.jwt-secret}")
    private String jwtSecret;

    @Value("${platform.security.jwt-expire-seconds}")
    private long jwtExpireSeconds;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.username())
                .claim("uid", user.userId())
                .claim("sid", user.studentId())
                .claim("role", user.role())
                .claim("studentNo", user.studentNo())
                .claim("name", user.name())
                .claim("major", user.major())
                .claim("grade", user.grade())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtExpireSeconds)))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public AuthenticatedUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Number sid = claims.get("sid", Number.class);
            return new AuthenticatedUser(
                    claims.get("uid", Number.class).longValue(),
                    sid != null ? sid.longValue() : null,
                    claims.getSubject(),
                    claims.get("role", String.class),
                    claims.get("studentNo", String.class),
                    claims.get("name", String.class),
                    claims.get("major", String.class),
                    claims.get("grade", String.class)
            );
        } catch (Exception ex) {
            throw new BusinessException("无效或过期的登录令牌");
        }
    }
}
