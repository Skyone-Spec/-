package edu.ruc.platform.auth.service;

public interface TokenBlocklistService {

    boolean isRevoked(String token);

    void revoke(String token);
}
