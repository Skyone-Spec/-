package edu.ruc.platform.worklog.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.student.domain.AdvisorScopeBinding;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.worklog.domain.StudentWorkLog;
import edu.ruc.platform.worklog.domain.StudentWorkLogActionLog;
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
import edu.ruc.platform.student.repository.StudentProfileRepository;
import edu.ruc.platform.worklog.repository.StudentWorkLogActionLogRepository;
import edu.ruc.platform.worklog.repository.StudentWorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class StudentWorkLogService implements StudentWorkLogApplicationService {

    private final StudentWorkLogRepository repository;
    private final StudentWorkLogActionLogRepository actionLogRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;

    @Override
    public StudentWorkLogResponse create(StudentWorkLogCreateRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateCreatePermission(user, request.studentId());
        StudentWorkLog entity = new StudentWorkLog();
        entity.setStudentId(request.studentId());
        entity.setStudentName(request.studentName());
        entity.setCategory(request.category());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setWorkloadScore(request.workloadScore());
        entity.setWorkDate(request.workDate());
        entity.setRecorderName(user.name());
        entity.setRecorderRole(user.role());
        StudentWorkLog saved = repository.save(entity);
        writeActionLog(saved.getId(), user, "CREATE", "创建工作记录");
        return toResponse(saved);
    }

    @Override
    public StudentWorkLogResponse update(Long id, StudentWorkLogUpdateRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentWorkLog entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工作记录不存在"));
        validateManagePermission(user, entity);
        entity.setCategory(request.category());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setWorkloadScore(request.workloadScore());
        entity.setWorkDate(request.workDate());
        entity = repository.save(entity);
        writeActionLog(entity.getId(), user, "UPDATE", "更新工作记录");
        return toResponse(entity);
    }

    @Override
    public void delete(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentWorkLog entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工作记录不存在"));
        validateManagePermission(user, entity);
        writeActionLog(entity.getId(), user, "DELETE", "删除工作记录");
        repository.delete(entity);
    }

    @Override
    public List<StudentWorkLogResponse> listByStudent(Long studentId) {
        validateStudentAccess(currentUserService.requireCurrentUser(), studentId);
        return repository.findByStudentIdOrderByWorkDateDescCreatedAtDesc(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<WorklogActionLogResponse> listActionLogs(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentWorkLog entity = repository.findById(id)
                .orElseThrow(() -> new BusinessException("工作记录不存在"));
        validateStudentAccess(user, entity.getStudentId());
        return actionLogRepository.findByWorkLogIdOrderByCreatedAtAsc(id).stream()
                .map(log -> new WorklogActionLogResponse(
                        log.getId(),
                        log.getWorkLogId(),
                        log.getOperatorName(),
                        log.getOperatorRole(),
                        log.getAction(),
                        log.getDetail(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public StudentWorkloadSummaryResponse summarize(Long studentId) {
        List<StudentWorkLogResponse> logs = listByStudent(studentId);
        int total = logs.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        String studentName = logs.isEmpty() ? "未知学生" : logs.get(0).studentName();
        return new StudentWorkloadSummaryResponse(studentId, studentName, logs.size(), total);
    }

    @Override
    public WorklogOverviewResponse overview() {
        List<StudentWorkLogResponse> logs = visibleLogs().stream().map(this::toResponse).toList();
        int totalWorkload = logs.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        List<WorklogCategoryStatsResponse> stats = logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudentWorkLogResponse::category))
                .entrySet()
                .stream()
                .map(entry -> new WorklogCategoryStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .toList();
        long totalStudents = logs.stream().map(StudentWorkLogResponse::studentId).distinct().count();
        return new WorklogOverviewResponse(logs.size(), (int) totalStudents, totalWorkload, stats);
    }

    @Override
    public List<StudentWorkLogResponse> filter(WorklogFilterRequest request, String sortBy, String sortDir) {
        validateFilterRequest(request);
        String normalizedCategory = QueryFilterSupport.trimToNull(request.category());
        String normalizedRecorderRole = QueryFilterSupport.normalizeUpper(request.recorderRole());
        java.util.Map<Long, edu.ruc.platform.student.domain.StudentProfile> studentMap = studentProfileRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        edu.ruc.platform.student.domain.StudentProfile::getId,
                        java.util.function.Function.identity()
                ));
        return sortLogs(visibleLogs().stream()
                .filter(item -> request.studentId() == null || item.getStudentId().equals(request.studentId()))
                .filter(item -> normalizedCategory == null || normalizedCategory.equalsIgnoreCase(item.getCategory()))
                .filter(item -> normalizedRecorderRole == null || normalizedRecorderRole.equalsIgnoreCase(item.getRecorderRole()))
                .filter(item -> request.grade() == null || request.grade().isBlank() || matchesGrade(studentMap.get(item.getStudentId()), request.grade()))
                .filter(item -> request.className() == null || request.className().isBlank() || matchesClassName(studentMap.get(item.getStudentId()), request.className()))
                .filter(item -> request.startDate() == null || !item.getWorkDate().isBefore(request.startDate()))
                .filter(item -> request.endDate() == null || !item.getWorkDate().isAfter(request.endDate()))
                .map(this::toResponse)
                .toList(), sortBy, sortDir);
    }

    @Override
    public WorklogAdminStatsResponse adminStats(WorklogFilterRequest request) {
        List<StudentWorkLogResponse> logs = filter(request, "workDate", "desc");
        int totalWorkload = logs.stream().mapToInt(StudentWorkLogResponse::workloadScore).sum();
        List<WorklogCategoryStatsResponse> categoryStats = logs.stream()
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
        List<WorklogStudentStatsResponse> topStudents = logs.stream()
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
        List<WorklogMonthStatsResponse> monthStats = logs.stream()
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
        List<WorklogDailyTrendResponse> dailyTrend = logs.stream()
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
        List<WorklogRecorderRoleStatsResponse> recorderRoleStats = logs.stream()
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
        List<WorklogScoreBandStatsResponse> scoreBandStats = logs.stream()
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
        java.util.Map<Long, edu.ruc.platform.student.domain.StudentProfile> studentMap = studentProfileRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        edu.ruc.platform.student.domain.StudentProfile::getId,
                        java.util.function.Function.identity()
                ));
        List<WorklogGradeStatsResponse> gradeStats = logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> {
                    var profile = studentMap.get(item.studentId());
                    return profile == null ? "UNKNOWN" : profile.getGrade();
                }))
                .entrySet()
                .stream()
                .map(entry -> new WorklogGradeStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogGradeStatsResponse::totalWorkloadScore).reversed())
                .toList();
        List<WorklogClassStatsResponse> classStats = logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> {
                    var profile = studentMap.get(item.studentId());
                    return profile == null ? "UNKNOWN" : profile.getClassName();
                }))
                .entrySet()
                .stream()
                .map(entry -> new WorklogClassStatsResponse(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(StudentWorkLogResponse::workloadScore).sum()
                ))
                .sorted(java.util.Comparator.comparing(WorklogClassStatsResponse::totalWorkloadScore).reversed())
                .toList();
        long totalStudents = logs.stream().map(StudentWorkLogResponse::studentId).distinct().count();
        return new WorklogAdminStatsResponse(logs.size(), (int) totalStudents, totalWorkload, categoryStats, topStudents, monthStats, dailyTrend, recorderRoleStats, scoreBandStats, gradeStats, classStats);
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

    private StudentWorkLogResponse toResponse(StudentWorkLog entity) {
        return new StudentWorkLogResponse(
                entity.getId(),
                entity.getStudentId(),
                entity.getStudentName(),
                entity.getCategory(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getWorkloadScore(),
                entity.getWorkDate(),
                entity.getRecorderName(),
                entity.getRecorderRole(),
                entity.getCreatedAt()
        );
    }

    private List<StudentWorkLog> visibleLogs() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return repository.findAll().stream()
                .filter(item -> canAccessStudent(user, item.getStudentId()))
                .toList();
    }

    private void validateCreatePermission(AuthenticatedUser user, Long studentId) {
        validateStudentAccess(user, studentId);
        if ("STUDENT".equals(user.role())) {
            throw new BusinessException("当前账号无权为该学生登记工作记录");
        }
    }

    private void validateManagePermission(AuthenticatedUser user, StudentWorkLog entity) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return;
        }
        validateStudentAccess(user, entity.getStudentId());
        if ("LEAGUE_SECRETARY".equals(user.role())) {
            if (!user.name().equals(entity.getRecorderName()) || !user.role().equals(entity.getRecorderRole())) {
                throw new BusinessException("当前账号无权修改其他记录人创建的工作记录");
            }
            return;
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            return;
        }
        throw new BusinessException("当前账号无权操作该工作记录");
    }

    private void validateStudentAccess(AuthenticatedUser user, Long studentId) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return;
        }
        studentDataScopeService.requireStudentAccess(user, studentId);
    }

    private boolean canAccessStudent(AuthenticatedUser user, Long studentId) {
        try {
            validateStudentAccess(user, studentId);
            return true;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void writeActionLog(Long workLogId, AuthenticatedUser user, String action, String detail) {
        StudentWorkLogActionLog log = new StudentWorkLogActionLog();
        log.setWorkLogId(workLogId);
        log.setOperatorId(user.userId());
        log.setOperatorName(user.name());
        log.setOperatorRole(user.role());
        log.setAction(action);
        log.setDetail(detail);
        actionLogRepository.save(log);
    }

    private boolean matchesGrade(edu.ruc.platform.student.domain.StudentProfile profile, String grade) {
        return profile != null && grade.equals(profile.getGrade());
    }

    private boolean matchesClassName(edu.ruc.platform.student.domain.StudentProfile profile, String className) {
        return profile != null && className.equals(profile.getClassName());
    }

    private void validateFilterRequest(WorklogFilterRequest request) {
        if (request == null) {
            return;
        }
        QueryFilterSupport.requireEnumValue(RoleType.class, request.recorderRole(), "记录人角色不支持: ");
    }

    private List<StudentWorkLogResponse> sortLogs(List<StudentWorkLogResponse> logs, String sortBy, String sortDir) {
        String normalizedSortBy = QueryFilterSupport.trimToNull(sortBy);
        String normalizedSortDir = QueryFilterSupport.trimToNull(sortDir);
        if (normalizedSortBy == null) {
            normalizedSortBy = "workDate";
        }
        if (normalizedSortDir == null) {
            normalizedSortDir = "desc";
        }
        if (!List.of("workDate", "workloadScore", "createdAt", "studentName").contains(normalizedSortBy)) {
            throw new BusinessException("不支持的排序字段");
        }
        if (!List.of("asc", "desc").contains(normalizedSortDir.toLowerCase())) {
            throw new BusinessException("不支持的排序方向");
        }
        java.util.Comparator<StudentWorkLogResponse> comparator = switch (normalizedSortBy) {
            case "workloadScore" -> java.util.Comparator.comparing(StudentWorkLogResponse::workloadScore);
            case "createdAt" -> java.util.Comparator.comparing(StudentWorkLogResponse::createdAt);
            case "studentName" -> java.util.Comparator.comparing(StudentWorkLogResponse::studentName);
            default -> java.util.Comparator.comparing(StudentWorkLogResponse::workDate);
        };
        if (!"asc".equalsIgnoreCase(normalizedSortDir)) {
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
}
