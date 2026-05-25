package edu.ruc.platform.student.dto;

import java.util.List;
import java.util.Map;

public final class StudentGrowthDtos {

    private StudentGrowthDtos() {
    }

    public record StudentGrowthModuleResponse(
            String moduleCode,
            String moduleName,
            String editMode,
            String editModeLabel
    ) {
    }

    public record StudentGrowthFieldItemResponse(
            String key,
            String label,
            String value
    ) {
    }

    public record StudentGrowthRecordRequest(
            Map<String, Object> fields
    ) {
    }

    public record StudentGrowthRecordResponse(
            Long id,
            String moduleCode,
            String moduleName,
            String editMode,
            String editModeLabel,
            String summary,
            String updatedAt,
            Map<String, String> rawFields,
            List<StudentGrowthFieldItemResponse> fields
    ) {
    }

    public record StudentGrowthModuleArchiveSectionResponse(
            String moduleCode,
            String moduleName,
            String editMode,
            String editModeLabel,
            List<StudentGrowthRecordResponse> records
    ) {
    }

    public record StudentGrowthArchiveResponse(
            StudentProfileResponse profile,
            List<StudentGrowthModuleArchiveSectionResponse> modules
    ) {
    }
}
