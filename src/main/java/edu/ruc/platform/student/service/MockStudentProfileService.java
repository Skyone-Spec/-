package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentPortraitCareerStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitFilterRequest;
import edu.ruc.platform.student.dto.StudentPortraitGpaBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitPageItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitRankBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentPortraitStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitUpsertRequest;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileStatsResponse;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryCreateRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockStudentProfileService implements StudentProfileApplicationService {

    private final CurrentUserService currentUserService;
    private final AtomicLong idGenerator = new AtomicLong(10010);
    private final AtomicLong statusHistoryIdGenerator = new AtomicLong(20);
    private final List<AdvisorScopeRecord> advisorScopes = new ArrayList<>(List.of(
            new AdvisorScopeRecord("advisor01", "王老师", "2023级", "计科一班", 10001L),
            new AdvisorScopeRecord("advisor02", "赵老师", "2023级", "计科二班", 10002L)
    ));
    private final List<StudentProfileResponse> students = new ArrayList<>(List.of(
            new StudentProfileResponse(10001L, "2023100001", "张三", "计算机类", "2023级", "计科一班", "advisor01|王老师", "本科", "zhangsan@example.edu", false, "ACTIVE", null, "****************12", "********5678", "北京*", "北京*", "导师*"),
            new StudentProfileResponse(10002L, "2023100002", "李四", "计算机类", "2023级", "计科二班", "advisor02|赵老师", "本科", "lisi@example.edu", false, "ACTIVE", null, "****************34", "********1234", "河北*", "河北*", "导师*")
    ));
    private final List<StudentPortraitResponse> portraits = new ArrayList<>(List.of(
            new StudentPortraitResponse(10001L, "男", "汉族", "国奖,校优", "国家奖学金", "数学建模", "支教实践", "20小时", "导师课题参与", null, "表现良好", 3.82, 12, 5, 98, "升学", "可作为榜样展示", "胡浩老师", "老师维护", true)
    ));
    private final List<StudentStatusHistoryResponse> statusHistories = new ArrayList<>(List.of(
            new StudentStatusHistoryResponse(21L, 10001L, null, "ACTIVE", null, "初始建档", "系统管理员", "SUPER_ADMIN", LocalDateTime.of(2026, 3, 20, 9, 0))
    ));

    @Override
    public List<StudentProfileResponse> listStudents() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return students.stream()
                .filter(item -> canAccessStudent(user, item))
                .map(item -> maskForRole(item, user))
                .toList();
    }

    @Override
    public List<StudentProfileResponse> listStudentsByScope(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return students.stream()
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> canAccessStudent(user, item))
                .map(item -> maskForRole(item, user))
                .toList();
    }

    @Override
    public StudentProfileResponse getStudent(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return students.stream()
                .filter(item -> item.id().equals(id))
                .filter(item -> canAccessStudent(user, item))
                .findFirst()
                .map(item -> maskForRole(item, user))
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
    }

    @Override
    public StudentProfileResponse currentStudentProfile() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.studentId() == null) {
            throw new BusinessException("当前账号未关联学生档案");
        }
        return students.stream()
                .filter(item -> item.id().equals(user.studentId()))
                .findFirst()
                .map(item -> maskForRole(item, user))
                .orElseThrow(() -> new BusinessException("当前学生信息不存在"));
    }

    @Override
    public List<StudentProfileResponse> listStudentsByGrade(String grade) {
        return listStudentsByScope(grade, null);
    }

    @Override
    public PageResponse<StudentProfileResponse> pageStudents(StudentProfileFilterRequest request, int page, int size) {
        List<StudentProfileResponse> filtered = filterStudents(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public StudentProfileStatsResponse studentStats(StudentProfileFilterRequest request) {
        List<StudentProfileResponse> filtered = filterStudents(request);
        return new StudentProfileStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> "ACTIVE".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> "GRADUATED".equals(item.status()) || Boolean.TRUE.equals(item.graduated())).count(),
                (int) filtered.stream().filter(item -> "SUSPENDED".equals(item.status())).count(),
                (int) filtered.stream().filter(item -> item.status() == null || item.status().isBlank()).count()
        );
    }

    @Override
    public StudentProfileResponse createStudent(StudentProfileUpsertRequest request) {
        validateStudentRequest(request);
        Long id = idGenerator.incrementAndGet();
        StudentProfileResponse response = toResponse(id, request);
        students.add(0, response);
        appendStatusHistory(id, null, request.status(), request.majorChangedTo(), "新建学生档案");
        return response;
    }

    @Override
    public StudentProfileResponse updateStudent(Long id, StudentProfileUpsertRequest request) {
        validateStudentRequest(request);
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).id().equals(id)) {
                StudentProfileResponse existing = students.get(i);
                StudentProfileResponse updated = toResponse(id, request);
                students.set(i, updated);
                recordStatusHistoryIfNeeded(existing, updated, "更新学生档案");
                return updated;
            }
        }
        throw new BusinessException("学生不存在");
    }

    @Override
    public List<StudentStatusHistoryResponse> listStatusHistory(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfileResponse profile = students.stream()
                .filter(item -> item.id().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, profile)) {
            throw new BusinessException("学生不存在或无权访问");
        }
        return statusHistories.stream()
                .filter(item -> item.studentId().equals(studentId))
                .sorted(java.util.Comparator.comparing(StudentStatusHistoryResponse::changedAt).reversed())
                .toList();
    }

    @Override
    public StudentStatusHistoryResponse createStatusHistory(Long studentId, StudentStatusHistoryCreateRequest request) {
        validateStatus(request.status(), request.changedToMajor());
        int studentIndex = -1;
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).id().equals(studentId)) {
                studentIndex = i;
                break;
            }
        }
        if (studentIndex < 0) {
            throw new BusinessException("学生不存在");
        }
        StudentProfileResponse current = students.get(studentIndex);
        StudentProfileResponse updated = new StudentProfileResponse(
                current.id(),
                current.studentNo(),
                current.name(),
                current.major(),
                current.grade(),
                current.className(),
                current.advisorScope(),
                current.degreeLevel(),
                current.email(),
                current.graduated(),
                request.status(),
                request.changedToMajor(),
                current.maskedIdCardNo(),
                current.maskedPhone(),
                current.maskedNativePlace(),
                current.maskedHouseholdAddress(),
                current.maskedSupervisor()
        );
        students.set(studentIndex, updated);
        return appendStatusHistory(studentId, current.status(), request.status(), request.changedToMajor(), request.reason());
    }

    @Override
    public StudentPortraitResponse getPortrait(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfileResponse profile = students.stream()
                .filter(item -> item.id().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, profile)
                && !"LEAGUE_SECRETARY".equals(user.role())
                && !("STUDENT".equals(user.role()) && studentId.equals(user.userId()))) {
            throw new BusinessException("学生不存在或无权访问");
        }
        return portraits.stream()
                .filter(item -> item.studentId().equals(studentId))
                .map(item -> enforcePortraitAccess(user, profile, item))
                .findFirst()
                .orElse(new StudentPortraitResponse(studentId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false));
    }

    @Override
    public StudentPortraitResponse upsertPortrait(Long studentId, StudentPortraitUpsertRequest request) {
        validatePortraitRequest(request);
        StudentPortraitResponse updated = new StudentPortraitResponse(
                studentId,
                request.gender(),
                request.ethnicity(),
                request.honors(),
                request.scholarships(),
                request.competitions(),
                request.socialPractice(),
                request.volunteerService(),
                request.researchExperience(),
                request.disciplineRecords(),
                request.dailyPerformance(),
                request.gpa(),
                request.gradeRank(),
                request.majorRank(),
                request.creditsEarned(),
                request.careerOrientation(),
                request.remarks(),
                request.updatedBy(),
                request.dataSource(),
                request.publicVisible()
        );
        for (int i = 0; i < portraits.size(); i++) {
            if (portraits.get(i).studentId().equals(studentId)) {
                portraits.set(i, updated);
                return updated;
            }
        }
        portraits.add(updated);
        return updated;
    }

    @Override
    public PageResponse<StudentPortraitPageItemResponse> pagePortraits(StudentPortraitFilterRequest request, int page, int size) {
        List<StudentPortraitPageItemResponse> filtered = filterPortraits(request);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, filtered.size());
        int toIndex = Math.min(fromIndex + normalizedSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) normalizedSize);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), totalPages, normalizedPage, normalizedSize);
    }

    @Override
    public StudentPortraitStatsResponse portraitStats(StudentPortraitFilterRequest request) {
        List<StudentPortraitPageItemResponse> filtered = filterPortraits(request);
        List<StudentPortraitCareerStatsResponse> careerStats = filtered.stream()
                .filter(item -> item.careerOrientation() != null && !item.careerOrientation().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(StudentPortraitPageItemResponse::careerOrientation))
                .entrySet()
                .stream()
                .map(entry -> new StudentPortraitCareerStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparing(StudentPortraitCareerStatsResponse::count).reversed())
                .toList();
        double averageGpa = filtered.stream()
                .filter(item -> item.gpa() != null)
                .mapToDouble(StudentPortraitPageItemResponse::gpa)
                .average()
                .orElse(0.0);
        List<StudentPortraitGpaBandStatsResponse> gpaBandStats = filtered.stream()
                .filter(item -> item.gpa() != null)
                .collect(java.util.stream.Collectors.groupingBy(item -> resolveGpaBand(item.gpa())))
                .entrySet()
                .stream()
                .map(entry -> new StudentPortraitGpaBandStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparingInt(item -> gpaBandOrder(item.band())))
                .toList();
        List<StudentPortraitRankBandStatsResponse> gradeRankBandStats = filtered.stream()
                .filter(item -> item.gradeRank() != null)
                .collect(java.util.stream.Collectors.groupingBy(item -> resolveRankBand(item.gradeRank())))
                .entrySet()
                .stream()
                .map(entry -> new StudentPortraitRankBandStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(java.util.Comparator.comparingInt(item -> rankBandOrder(item.band())))
                .toList();
        return new StudentPortraitStatsResponse(
                filtered.size(),
                (int) filtered.stream().filter(item -> Boolean.TRUE.equals(item.publicVisible())).count(),
                averageGpa,
                careerStats,
                gpaBandStats,
                gradeRankBandStats
        );
    }

    private StudentProfileResponse toResponse(Long id, StudentProfileUpsertRequest request) {
        return new StudentProfileResponse(
                id,
                request.studentNo(),
                request.name(),
                request.major(),
                request.grade(),
                request.className(),
                request.advisorScope(),
                request.degreeLevel(),
                request.email(),
                request.graduated(),
                request.status(),
                request.majorChangedTo(),
                mask(request.encryptedIdCardNo()),
                mask(request.encryptedPhone()),
                mask(request.encryptedNativePlace()),
                mask(request.encryptedHouseholdAddress()),
                mask(request.encryptedSupervisor())
        );
    }

    private void validateStudentRequest(StudentProfileUpsertRequest request) {
        validateStatus(request.status(), request.majorChangedTo());
        if (Boolean.TRUE.equals(request.graduated()) && !"GRADUATED".equals(request.status())) {
            throw new BusinessException("毕业状态为 true 时学生状态必须为 GRADUATED");
        }
        if (Boolean.FALSE.equals(request.graduated()) && "GRADUATED".equals(request.status())) {
            throw new BusinessException("学生状态为 GRADUATED 时毕业状态必须为 true");
        }
        validateEncryptedSensitiveField("encryptedIdCardNo", request.encryptedIdCardNo(), "(^\\d{15}$)|(^\\d{17}[0-9Xx]$)", "身份证号");
        validateEncryptedSensitiveField("encryptedPhone", request.encryptedPhone(), "^1\\d{10}$", "手机号");
    }

    private void validateStatus(String status, String changedToMajor) {
        if (status == null || status.isBlank()) {
            throw new BusinessException("学生状态不能为空");
        }
        List<String> allowedStatuses = List.of("ACTIVE", "SUSPENDED", "GRADUATED", "TRANSFERRED", "WITHDRAWN");
        if (!allowedStatuses.contains(status)) {
            throw new BusinessException("学生状态仅支持 ACTIVE、SUSPENDED、GRADUATED、TRANSFERRED、WITHDRAWN");
        }
        if ((status.equals("TRANSFERRED") || status.equals("WITHDRAWN")) && (changedToMajor == null || changedToMajor.isBlank())) {
            throw new BusinessException("转专业或转出状态必须填写变更后专业");
        }
    }

    private void validatePortraitRequest(StudentPortraitUpsertRequest request) {
        if (request.gpa() != null && (request.gpa() < 0 || request.gpa() > 4.5)) {
            throw new BusinessException("学生画像 GPA 必须在 0 到 4.5 之间");
        }
        if (request.gradeRank() != null && request.gradeRank() <= 0) {
            throw new BusinessException("年级排名必须大于 0");
        }
        if (request.majorRank() != null && request.majorRank() <= 0) {
            throw new BusinessException("专业排名必须大于 0");
        }
        if (request.creditsEarned() != null && request.creditsEarned() < 0) {
            throw new BusinessException("已修学分不能小于 0");
        }
        if (request.updatedBy() == null || request.updatedBy().isBlank()) {
            AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
            if (RoleType.SUPER_ADMIN.name().equals(currentUser.role())
                    || RoleType.COLLEGE_ADMIN.name().equals(currentUser.role())
                    || RoleType.COUNSELOR.name().equals(currentUser.role())) {
                throw new BusinessException("维护人不能为空");
            }
        }
    }

    private void recordStatusHistoryIfNeeded(StudentProfileResponse before,
                                             StudentProfileResponse after,
                                             String reason) {
        boolean statusChanged = !java.util.Objects.equals(before.status(), after.status());
        boolean majorChanged = !java.util.Objects.equals(before.majorChangedTo(), after.majorChangedTo());
        if (!statusChanged && !majorChanged) {
            return;
        }
        appendStatusHistory(after.id(), before.status(), after.status(), after.majorChangedTo(), reason);
    }

    private StudentStatusHistoryResponse appendStatusHistory(Long studentId,
                                                             String fromStatus,
                                                             String toStatus,
                                                             String changedToMajor,
                                                             String reason) {
        if (toStatus == null || toStatus.isBlank()) {
            return null;
        }
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentStatusHistoryResponse response = new StudentStatusHistoryResponse(
                statusHistoryIdGenerator.incrementAndGet(),
                studentId,
                fromStatus,
                toStatus,
                changedToMajor,
                reason,
                user.name(),
                user.role(),
                LocalDateTime.now()
        );
        statusHistories.add(0, response);
        return response;
    }

    private StudentProfileResponse maskForRole(StudentProfileResponse item, AuthenticatedUser user) {
        String role = user.role();
        if ("SUPER_ADMIN".equals(role) || "COLLEGE_ADMIN".equals(role) || "COUNSELOR".equals(role)) {
            return item;
        }
        if ("CLASS_ADVISOR".equals(role)) {
            return new StudentProfileResponse(
                    item.id(),
                    item.studentNo(),
                    item.name(),
                    item.major(),
                    item.grade(),
                    item.className(),
                    item.advisorScope(),
                    item.degreeLevel(),
                    item.email(),
                    item.graduated(),
                    item.status(),
                    item.majorChangedTo(),
                    item.maskedIdCardNo(),
                    item.maskedPhone(),
                    null,
                    null,
                    null
            );
        }
        if ("LEAGUE_SECRETARY".equals(role)) {
            return new StudentProfileResponse(
                    item.id(),
                    item.studentNo(),
                    item.name(),
                    item.major(),
                    item.grade(),
                    item.className(),
                    item.advisorScope(),
                    item.degreeLevel(),
                    null,
                    item.graduated(),
                    item.status(),
                    item.majorChangedTo(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        if ("STUDENT".equals(role) && item.id().equals(user.userId())) {
            return item;
        }
        return new StudentProfileResponse(
                item.id(),
                item.studentNo(),
                item.name(),
                item.major(),
                item.grade(),
                item.className(),
                item.advisorScope(),
                item.degreeLevel(),
                null,
                item.graduated(),
                item.status(),
                item.majorChangedTo(),
                null,
                null,
                null,
                null,
                null
        );
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "*".repeat(Math.max(4, Math.min(16, value.length())));
    }

    private void validateEncryptedSensitiveField(String fieldName,
                                                 String value,
                                                 String plaintextPattern,
                                                 String label) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (value.matches(plaintextPattern)) {
            throw new BusinessException(fieldName + " 不能直接提交明文" + label + "，请传入加密后内容");
        }
    }

    private boolean canAccessStudent(AuthenticatedUser user, StudentProfileResponse item) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return true;
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role())) {
            return user.grade() != null && user.grade().equals(item.grade());
        }
        if ("CLASS_ADVISOR".equals(user.role())) {
            if (user.grade() == null || user.grade().isBlank() || !user.grade().equals(item.grade())) {
                return false;
            }
            List<Long> studentIds = advisorScopes.stream()
                    .filter(scope -> scope.advisorUsername().equals(user.username()) || scope.advisorName().equals(user.name()))
                    .map(AdvisorScopeRecord::studentId)
                    .distinct()
                    .toList();
            if (!studentIds.isEmpty()) {
                return studentIds.contains(item.id());
            }
            if (item.advisorScope() == null || item.advisorScope().isBlank()) {
                return true;
            }
            return item.advisorScope().contains(user.username()) || item.advisorScope().contains(user.name());
        }
        return "STUDENT".equals(user.role()) && item.id().equals(user.userId());
    }

    private StudentPortraitResponse enforcePortraitAccess(AuthenticatedUser user,
                                                          StudentProfileResponse profile,
                                                          StudentPortraitResponse portrait) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return portrait;
        }
        if ("CLASS_ADVISOR".equals(user.role()) && canAccessStudent(user, profile)) {
            return portrait;
        }
        if ("STUDENT".equals(user.role()) && profile.id().equals(user.userId())) {
            return portrait;
        }
        if (("LEAGUE_SECRETARY".equals(user.role()) || "CLASS_LEADER".equals(user.role()))
                && user.grade() != null
                && user.grade().equals(profile.grade())
                && Boolean.TRUE.equals(portrait.publicVisible())) {
            return portrait;
        }
        throw new BusinessException("当前账号无权查看该学生画像");
    }

    private record AdvisorScopeRecord(
            String advisorUsername,
            String advisorName,
            String grade,
            String className,
            Long studentId
    ) {
    }

    private List<StudentPortraitPageItemResponse> filterPortraits(StudentPortraitFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedGrade = QueryFilterSupport.trimToNull(request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request.className());
        String normalizedCareerOrientation = QueryFilterSupport.trimToNull(request.careerOrientation());
        return students.stream()
                .filter(item -> canAccessStudent(user, item))
                .map(profile -> {
                    StudentPortraitResponse portrait = portraits.stream()
                            .filter(item -> item.studentId().equals(profile.id()))
                            .findFirst()
                            .orElse(null);
                    if (portrait == null) {
                        return null;
                    }
                    return new StudentPortraitPageItemResponse(
                            profile.id(),
                            profile.studentNo(),
                            profile.name(),
                            profile.grade(),
                            profile.className(),
                            portrait.gpa(),
                            portrait.gradeRank(),
                            portrait.majorRank(),
                            portrait.creditsEarned(),
                            portrait.careerOrientation(),
                            portrait.publicVisible(),
                            portrait.updatedBy()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> request.publicVisible() == null || request.publicVisible().equals(item.publicVisible()))
                .filter(item -> normalizedCareerOrientation == null || normalizedCareerOrientation.equals(item.careerOrientation()))
                .filter(item -> request.minGpa() == null || (item.gpa() != null && item.gpa() >= request.minGpa()))
                .toList();
    }

    private List<StudentProfileResponse> filterStudents(StudentProfileFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentFilterRequest(request);
        String normalizedGrade = QueryFilterSupport.trimToNull(request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request.className());
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return students.stream()
                .filter(item -> canAccessStudent(user, item))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.name(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.major(), normalizedKeyword))
                .map(item -> maskForRole(item, user))
                .toList();
    }

    private void validateStudentFilterRequest(StudentProfileFilterRequest request) {
        if (request == null) {
            return;
        }
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        if (normalizedStatus == null) {
            return;
        }
        List<String> allowedStatuses = List.of("ACTIVE", "SUSPENDED", "GRADUATED", "TRANSFERRED", "WITHDRAWN");
        if (!allowedStatuses.contains(normalizedStatus)) {
            throw new BusinessException("学生状态仅支持 ACTIVE、SUSPENDED、GRADUATED、TRANSFERRED、WITHDRAWN");
        }
    }

    private String resolveGpaBand(Double gpa) {
        if (gpa == null) {
            return "UNKNOWN";
        }
        if (gpa < 2.0) {
            return "<2.0";
        }
        if (gpa < 3.0) {
            return "2.0-2.99";
        }
        if (gpa < 3.5) {
            return "3.0-3.49";
        }
        return "3.5+";
    }

    private int gpaBandOrder(String band) {
        return switch (band) {
            case "<2.0" -> 0;
            case "2.0-2.99" -> 1;
            case "3.0-3.49" -> 2;
            case "3.5+" -> 3;
            default -> 4;
        };
    }

    private String resolveRankBand(Integer rank) {
        if (rank == null || rank <= 0) {
            return "UNKNOWN";
        }
        if (rank <= 10) {
            return "TOP10";
        }
        if (rank <= 30) {
            return "TOP30";
        }
        if (rank <= 50) {
            return "TOP50";
        }
        return "50+";
    }

    private int rankBandOrder(String band) {
        return switch (band) {
            case "TOP10" -> 0;
            case "TOP30" -> 1;
            case "TOP50" -> 2;
            case "50+" -> 3;
            default -> 4;
        };
    }
}
