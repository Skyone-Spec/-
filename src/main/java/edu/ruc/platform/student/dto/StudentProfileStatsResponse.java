package edu.ruc.platform.student.dto;

public record StudentProfileStatsResponse(
        int totalStudents,
        int activeStudents,
        int graduatedStudents,
        int suspendedStudents,
        int statusUnknownStudents
) {
}
