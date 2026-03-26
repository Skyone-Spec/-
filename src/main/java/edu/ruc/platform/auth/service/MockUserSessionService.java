package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockUserSessionService implements UserSessionService {

    @Override
    public void onLogin(String token, AuthenticatedUser user) {
    }

    @Override
    public void onLogout(String token) {
    }
}
