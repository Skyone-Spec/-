package edu.ruc.platform.platform.dto;

public record PlatformStudentQueryResponse(
        Long studentId,
        String studentNo,
        String name,
        String major,
        String grade,
        String className,
        String degreeLevel,
        String status,
        Boolean graduated,
        String email,
        SensitiveFields sensitiveFields
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
