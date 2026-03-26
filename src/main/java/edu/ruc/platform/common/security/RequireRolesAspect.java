package edu.ruc.platform.common.security;

import edu.ruc.platform.auth.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireRolesAspect {

    private final CurrentUserService currentUserService;

    @Before("@within(requireRoles) || @annotation(requireRoles)")
    public void checkRole(RequireRoles requireRoles) {
        currentUserService.requireAnyRole(requireRoles.value());
    }
}
