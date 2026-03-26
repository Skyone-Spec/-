package edu.ruc.platform.worklog.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.worklog.dto.StudentWorkLogCreateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkLogResponse;
import edu.ruc.platform.worklog.dto.StudentWorkLogUpdateRequest;
import edu.ruc.platform.worklog.dto.StudentWorkloadSummaryResponse;
import edu.ruc.platform.worklog.dto.WorklogAdminStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogActionLogResponse;
import edu.ruc.platform.worklog.dto.WorklogClassStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogDailyTrendResponse;
import edu.ruc.platform.worklog.dto.WorklogStudentStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogFilterRequest;
import edu.ruc.platform.worklog.dto.WorklogCategoryStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogGradeStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogExportFieldResponse;
import edu.ruc.platform.worklog.dto.WorklogExportMetadataResponse;
import edu.ruc.platform.worklog.dto.WorklogMonthStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogOverviewResponse;
import edu.ruc.platform.worklog.dto.WorklogRecorderRoleStatsResponse;
import edu.ruc.platform.worklog.dto.WorklogScoreBandStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockStudentWorkLogService implements StudentWorkLogApplicationService {

    private final CurrentUserService currentUserService;
    private final AtomicLong idGenerator = new AtomicLong(502);
    private final AtomicLong actionLogIdGenerator = new AtomicLong(902);
    private final List<StudentWorkLogResponse> logs = new ArrayList<>(List.of(
            new StudentWorkLogResponse(501L, 10001L, "张三", "党团事务", "党课学习小组签到组织", "负责签到与材料整理", 3, LocalDate.of(2026, 3, 10), "胡浩老师", "COUNSELOR", LocalDateTime.of(2026, 3, 10, 18, 0)),
            new StudentWorkLogResponse(502L, 10001L, "张三", "学生工作", "活动通知收集", "汇总班级反馈情况", 2, LocalDate.of(2026, 3, 15), "李四", "LEAGUE_SECRETARY", LocalDateTime.of(2026, 3, 15, 20, 0))
    ));
    private final List<WorklogActionLogResponse> actionLogs = new ArrayList<>(List.of(
            new WorklogActionLogResponse(901L, 501L, "胡浩老师", "COUNSELOR", "CREATE", "创建工作记录", LocalDateTime.of(2026, 3, 10, 18, 0)),
            new WorklogActionLogResponse(902L, 502L, "李四", "LEAGUE_SECRETARY", "CREATE", "创建工作记录", LocalDateTime.of(2026, 3, 15, 20, 0))
    ));

    @Override
    public StudentWorkLogResponse create(StudentWorkLogCreateRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateCreatePermission(user, request.studentId());
        StudentWorkLogResponse response = new StudentWorkLogResponse(
                idGenerator.incrementAndGet(),
                request.studentId(),
                request.studentName(),
                request.category(),
                request.title(),
                request.description(),
                request.workloadScore(),
                request.workDate(),
                user.name(),
                user.role(),
                LocalDateTime.now()
        );
        logs.add(0, response);
        writeActionLog(response.id(), user, "CREATE", "创建工作记录");
        return response;
    }

    @Override
    public StudentWorkLogResponse update(Long id, StudentWorkLogUpdateRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        for (int i = 0; i < logs.size(); i++) {
            StudentWorkLogResponse item = logs.get(i);
            if (!item.id().equals(id)) {
                continue;
            }
            validateManagePermission(user, item);
            StudentWorkLogResponse updated = new StudentWorkLogResponse(
                    item.id(),
                    item.studentId(),
                    item.studentName(),
                    request.category(),
                    request.title(),
                    request.description(),
                    request.workloadScore(),
                    request.workDate(),
                    item.recorderName(),
                    item.recorderRole(),
                    item.createdAt()
            );
            logs.set(i, updated);
            writeActionLog(id, user, "UPDATE", "更新工作记录");
            return updated;
        }
        throw new BusinessException("工作记录不存在");
    }

    @Override
    public void delete(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentWorkLogResponse target = logs.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("工作记录不存在"));
        validateManagePermission(user, target);
        writeActionLog(id, user, "DELETE", "删除工作记录");
        logs.removeIf(item -> item.id().equals(id));
    }

    @Override
    public List<StudentWorkLogResponse> listByStudent(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentAccess(user, studentId);
        return scopedLogs(user).stream()
                .filter(item -> item.studentId().equals(studentId))
                .toList();
    }

    @Override
    public List<WorklogActionLogResponse> listActionLogs(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentWorkLogResponse target = logs.stream()
                .filter(item -> item.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("工作记录不存在"));
        validateStudentAccess(user, target.studentId());
        return actionLogs.stream()
                .filter(item -> item.workLogId().equals(id))
                .sorted(Comparator.comparing(WorklogActionLogResponse::operatedAt))
                .toList();
    }

    @Override
    public StudentWorkloadSummaryResponse summarize(Long studentId) {
        List<StudentWorkLogResponse> studentLogs = listByStudent(studentId);
        int total = studentLogs.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        String studentName = studentLogs.isEmpty() ? "未知学生" : studentLogs.get(0).studentName();
        return new StudentWorkloadSummaryResponse(studentId, studentName, studentLogs.size(), total);
    }

    @Override
    public WorklogOverviewResponse overview() {
        List<StudentWorkLogResponse> visibleLogs = scopedLogs(currentUserService.requireCurrentUser());
        int totalWorkload = visibleLogs.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        List<WorklogCategoryStatsResponse> stats = visibleLogs.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudentWorkLogResponse::category))
                .entrySet()
                .stream()
                .map(entry -> new WorklogCategoryStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .toList();
        long totalStudents = visibleLogs.stream().map(StudentWorkLogResponse::studentId).distinct().count();
        return new WorklogOverviewResponse(visibleLogs.size(), (int) totalStudents, totalWorkload, stats);
    }

    @Override
    public List<StudentWorkLogResponse> filter(WorklogFilterRequest request, String sortBy, String sortDir) {
        return sortLogs(scopedLogs(currentUserService.requireCurrentUser()).stream()
                .filter(item -> request.studentId() == null || item.studentId().equals(request.studentId()))
                .filter(item -> request.category() == null || request.category().equals(item.category()))
                .filter(item -> request.recorderRole() == null || request.recorderRole().equals(item.recorderRole()))
                .filter(item -> request.grade() == null || request.grade().isBlank() || request.grade().equals(resolveProfile(item.studentId()).grade()))
                .filter(item -> request.className() == null || request.className().isBlank() || request.className().equals(resolveProfile(item.studentId()).className()))
                .filter(item -> request.startDate() == null || !item.workDate().isBefore(request.startDate()))
                .filter(item -> request.endDate() == null || !item.workDate().isAfter(request.endDate()))
                .toList(), sortBy, sortDir);
    }

    @Override
    public WorklogAdminStatsResponse adminStats(WorklogFilterRequest request) {
        List<StudentWorkLogResponse> filtered = filter(request, "workDate", "desc");
        int totalWorkload = filtered.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        List<WorklogCategoryStatsResponse> categoryStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudentWorkLogResponse::category))
                .entrySet()
                .stream()
                .map(entry -> new WorklogCategoryStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogCategoryStatsResponse::totalWorkloadScore).reversed())
                .toList();
        List<WorklogStudentStatsResponse> topStudents = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        StudentWorkLogResponse::studentId,
                        java.util.stream.Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new WorklogStudentStatsResponse(
                        entry.getKey(),
                        entry.getValue().get(0).studentName(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogStudentStatsResponse::totalWorkloadScore).reversed())
                .limit(10)
                .toList();
        List<WorklogMonthStatsResponse> monthStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> item.workDate().getYear() + "-" + String.format("%02d", item.workDate().getMonthValue())
                ))
                .entrySet()
                .stream()
                .map(entry -> new WorklogMonthStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogMonthStatsResponse::month))
                .toList();
        List<WorklogDailyTrendResponse> dailyTrend = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> item.workDate().toString()))
                .entrySet()
                .stream()
                .map(entry -> new WorklogDailyTrendResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogDailyTrendResponse::date))
                .toList();
        List<WorklogRecorderRoleStatsResponse> recorderRoleStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudentWorkLogResponse::recorderRole))
                .entrySet()
                .stream()
                .map(entry -> new WorklogRecorderRoleStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogRecorderRoleStatsResponse::totalWorkloadScore).reversed())
                .toList();
        List<WorklogScoreBandStatsResponse> scoreBandStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> resolveScoreBand(item.workloadScore())))
                .entrySet()
                .stream()
                .map(entry -> new WorklogScoreBandStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparingInt(item -> scoreBandOrder(item.band())))
                .toList();
        List<WorklogGradeStatsResponse> gradeStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> "2023级"))
                .entrySet()
                .stream()
                .map(entry -> new WorklogGradeStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .toList();
        List<WorklogClassStatsResponse> classStats = filtered.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> item.studentId().equals(10001L) ? "计科一班" : "计科二班"))
                .entrySet()
                .stream()
                .map(entry -> new WorklogClassStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .toList();
        long totalStudents = filtered.stream().map(StudentWorkLogResponse::studentId).distinct().count();
        return new WorklogAdminStatsResponse(filtered.size(), (int) totalStudents, totalWorkload, categoryStats, topStudents, monthStats, dailyTrend, recorderRoleStats, scoreBandStats, gradeStats, classStats);
    }

    @Override
    public PageResponse<StudentWorkLogResponse> adminPage(WorklogFilterRequest request, int page, int size, String sortBy, String sortDir) {
        List<StudentWorkLogResponse> filtered = filter(request, sortBy, sortDir);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public WorklogExportMetadataResponse exportMetadata() {
        return new WorklogExportMetadataResponse(
                "student-worklogs-export",
                List.of(
                        new WorklogExportFieldResponse("studentId", "学生ID"),
                        new WorklogExportFieldResponse("studentName", "学生姓名"),
                        new WorklogExportFieldResponse("category", "工作分类"),
                        new WorklogExportFieldResponse("title", "工作标题"),
                        new WorklogExportFieldResponse("workloadScore", "工作量分值"),
                        new WorklogExportFieldResponse("workDate", "工作日期"),
                        new WorklogExportFieldResponse("recorderName", "记录人"),
                        new WorklogExportFieldResponse("recorderRole", "记录人角色")
                )
        );
    }

    private void validateCreatePermission(AuthenticatedUser user, Long studentId) {
        validateStudentAccess(user, studentId);
        if ("STUDENT".equals(user.role())) {
            throw new BusinessException("当前账号无权为该学生登记工作记录");
        }
    }

    private void validateManagePermission(AuthenticatedUser user, StudentWorkLogResponse target) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return;
        }
        if ("LEAGUE_SECRETARY".equals(user.role())) {
            if (!target.recorderName().equals(user.name()) && !target.recorderRole().equals(user.role())) {
                throw new BusinessException("当前账号无权修改其他记录人创建的工作记录");
            }
            validateStudentAccess(user, target.studentId());
            return;
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            validateStudentAccess(user, target.studentId());
            return;
        }
        throw new BusinessException("当前账号无权操作该工作记录");
    }

    private void validateStudentAccess(AuthenticatedUser user, Long studentId) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return;
        }
        MockStudentProfile profile = resolveProfile(studentId);
        if (profile == null) {
            throw new BusinessException("学生不存在或无权访问");
        }
        if ("STUDENT".equals(user.role())) {
            if (studentId.equals(user.userId())) {
                return;
            }
            throw new BusinessException("学生不存在或无权访问");
        }
        if ("LEAGUE_SECRETARY".equals(user.role())) {
            if (user.grade() != null && user.grade().equals(profile.grade())) {
                return;
            }
            throw new BusinessException("学生不存在或无权访问");
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            if (user.grade() != null && user.grade().equals(profile.grade())
                    && (("advisor01".equals(user.username()) && "计科一班".equals(profile.className()))
                    || ("advisor02".equals(user.username()) && "计科二班".equals(profile.className())))) {
                return;
            }
            throw new BusinessException("学生不存在或无权访问");
        }
        throw new BusinessException("学生不存在或无权访问");
    }

    private List<StudentWorkLogResponse> scopedLogs(AuthenticatedUser user) {
        return logs.stream()
                .filter(item -> canAccessStudent(user, item.studentId()))
                .toList();
    }

    private boolean canAccessStudent(AuthenticatedUser user, Long studentId) {
        try {
            validateStudentAccess(user, studentId);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private MockStudentProfile resolveProfile(Long studentId) {
        if (studentId.equals(10001L)) {
            return new MockStudentProfile(10001L, "2023级", "计科一班");
        }
        if (studentId.equals(10002L)) {
            return new MockStudentProfile(10002L, "2023级", "计科二班");
        }
        return null;
    }

    private void writeActionLog(Long workLogId, AuthenticatedUser user, String action, String detail) {
        actionLogs.add(new WorklogActionLogResponse(
                actionLogIdGenerator.incrementAndGet(),
                workLogId,
                user.name(),
                user.role(),
                action,
                detail,
                LocalDateTime.now()
        ));
    }

    private List<StudentWorkLogResponse> sortLogs(List<StudentWorkLogResponse> logs, String sortBy, String sortDir) {
        if (!List.of("workDate", "workloadScore", "createdAt", "studentName").contains(sortBy)) {
            throw new BusinessException("不支持的排序字段");
        }
        if (!List.of("asc", "desc").contains(sortDir.toLowerCase())) {
            throw new BusinessException("不支持的排序方向");
        }
        java.util.Comparator<StudentWorkLogResponse> comparator = switch (sortBy) {
            case "workloadScore" -> java.util.Comparator.comparing(StudentWorkLogResponse::workloadScore);
            case "createdAt" -> java.util.Comparator.comparing(StudentWorkLogResponse::createdAt);
            case "studentName" -> java.util.Comparator.comparing(StudentWorkLogResponse::studentName);
            default -> java.util.Comparator.comparing(StudentWorkLogResponse::workDate);
        };
        if (!"asc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return logs.stream().sorted(comparator).toList();
    }

    private String resolveScoreBand(Integer score) {
        if (score == null || score <= 0) {
            return "0";
        }
        if (score <= 2) {
            return "1-2";
        }
        if (score <= 4) {
            return "3-4";
        }
        if (score <= 6) {
            return "5-6";
        }
        return "7+";
    }

    private int scoreBandOrder(String band) {
        return switch (band) {
            case "0" -> 0;
            case "1-2" -> 1;
            case "3-4" -> 2;
            case "5-6" -> 3;
            default -> 4;
        };
    }

    private record MockStudentProfile(Long id, String grade, String className) {
    }
}
