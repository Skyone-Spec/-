package edu.ruc.platform.academic.dto;

public record AcademicProgramModuleResponse(
        Long id,
        String moduleCode,
        String moduleName,
        String moduleType,
        Integer requiredCredits,
        String description,
        Integer sortOrder
) {}
