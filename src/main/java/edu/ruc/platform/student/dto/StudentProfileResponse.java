package edu.ruc.platform.student.dto;

public record StudentProfileResponse(
        Long id,
        String studentNo,
        String name,
        String major,
        String grade,
        String className,
        String advisorScope,
        String degreeLevel,
        String email,
        Boolean graduated,
        String status,
        String majorChangedTo,
        String maskedIdCardNo,
        String maskedPhone,
        String maskedNativePlace,
        String maskedHouseholdAddress,
        String maskedSupervisor
) {
}
