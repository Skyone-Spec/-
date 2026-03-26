package edu.ruc.platform.platform.dto;

import java.util.List;

public record PlatformRoleResponse(
        String role,
        String description,
        List<String> dataScopes
) {
}
