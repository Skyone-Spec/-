package edu.ruc.platform.academic.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AcademicProgramResponse(
        Long id,
        String programCode,
        String programName,
        String major,
        String grade,
        Integer totalCredits,
        String description,
        Boolean isActive,
        List<AcademicProgramModuleResponse> modules,
        LocalDateTime createdAt
) {}
