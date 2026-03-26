package edu.ruc.platform.platform.service;

import edu.ruc.platform.platform.dto.PlatformSecurityPolicyResponse;
import edu.ruc.platform.platform.dto.PlatformSecurityPolicyUpdateRequest;

public interface PlatformSecurityPolicyService {

    PlatformSecurityPolicyResponse getPolicy();

    PlatformSecurityPolicyResponse updatePolicy(PlatformSecurityPolicyUpdateRequest request);

    int maxFailedLoginAttempts();

    int lockDurationMinutes();

    String defaultPassword();

    boolean requirePasswordResetOnCreate();
}
