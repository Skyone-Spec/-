package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;

public interface TokenService {

    String generateToken(AuthenticatedUser user);

    AuthenticatedUser parseToken(String token);
}
