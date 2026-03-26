package edu.ruc.platform.certificate.dto;

public record ApprovalTaskStatsResponse(
        int totalTasks,
        int pendingTasks,
        int counselorApprovedTasks,
        int approvedTasks,
        int rejectedTasks,
        int withdrawnTasks
) {
}
