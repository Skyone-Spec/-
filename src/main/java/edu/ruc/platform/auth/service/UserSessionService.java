package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;

public interface UserSessionService {

    void onLogin(String token, AuthenticatedUser user);

    void onLogout(String token);
}
