package edu.ruc.platform.platform.support;

import edu.ruc.platform.common.enums.StudentActionType;

import java.util.Map;

public final class StudentActionPathRegistry {

    public static final Map<String, String> PATHS = Map.of(
            StudentActionType.PARTY_REMINDER.name(), "/student/party-progress",
            StudentActionType.CERTIFICATE.name(), "/student/certificates",
            StudentActionType.ACADEMIC.name(), "/student/academic-analysis",
            StudentActionType.WORKLOG.name(), "/student/worklogs",
            StudentActionType.PORTRAIT.name(), "/student/portrait",
            StudentActionType.NOTICE.name(), "/student/notices"
    );

    private StudentActionPathRegistry() {
    }

    public static String resolve(StudentActionType type) {
        return PATHS.getOrDefault(type.name(), "/student");
    }
}
