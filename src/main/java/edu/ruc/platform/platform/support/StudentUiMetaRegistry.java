package edu.ruc.platform.platform.support;

import edu.ruc.platform.common.enums.StudentActionPriority;
import edu.ruc.platform.common.enums.StudentActionType;
import edu.ruc.platform.platform.dto.PlatformStudentUiActionMetaResponse;

import java.util.List;

public final class StudentUiMetaRegistry {

    private StudentUiMetaRegistry() {
    }

    public static List<PlatformStudentUiActionMetaResponse> all() {
        return List.of(
                item(StudentActionType.PARTY_REMINDER, StudentActionPriority.MEDIUM, "party-progress", "blue"),
                item(StudentActionType.CERTIFICATE, StudentActionPriority.HIGH, "certificate", "orange"),
                item(StudentActionType.ACADEMIC, StudentActionPriority.HIGH, "academic-warning", "red"),
                item(StudentActionType.WORKLOG, StudentActionPriority.MEDIUM, "worklog", "teal"),
                item(StudentActionType.PORTRAIT, StudentActionPriority.LOW, "portrait", "green"),
                item(StudentActionType.NOTICE, StudentActionPriority.MEDIUM, "notice", "slate")
        );
    }

    private static PlatformStudentUiActionMetaResponse item(StudentActionType type,
                                                            StudentActionPriority priority,
                                                            String iconKey,
                                                            String colorKey) {
        return new PlatformStudentUiActionMetaResponse(
                type.name(),
                priority.name(),
                StudentActionPathRegistry.resolve(type),
                iconKey,
                colorKey
        );
    }
}
