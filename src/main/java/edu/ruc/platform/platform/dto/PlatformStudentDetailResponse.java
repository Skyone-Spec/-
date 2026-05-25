package edu.ruc.platform.platform.dto;

import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleArchiveSectionResponse;

import java.util.List;

public record PlatformStudentDetailResponse(
        Long studentId,
        String studentNo,
        String name,
        String collegeName,
        String major,
        String grade,
        String className,
        String advisorScope,
        String degreeLevel,
        String status,
        Boolean graduated,
        String majorChangedTo,
        String email,
        SensitiveFields sensitiveFields,
        List<StudentGrowthModuleArchiveSectionResponse> growthModules
) {
    public record SensitiveFields(
            String maskedIdCardNo,
            String maskedPhone,
            String maskedNativePlace,
            String maskedHouseholdAddress,
            String maskedSupervisor
    ) {
    }
}
