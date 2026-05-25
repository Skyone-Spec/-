package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthArchiveResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthFieldItemResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleArchiveSectionResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordRequest;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordResponse;
import edu.ruc.platform.student.support.StudentGrowthModuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockStudentGrowthService implements StudentGrowthApplicationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CurrentUserService currentUserService;
    private final StudentProfileApplicationService studentProfileService;
    private final AtomicLong idGenerator = new AtomicLong(1000);
    private final List<MockGrowthRecord> records = new ArrayList<>(List.of(
            new MockGrowthRecord(101L, 10001L, StudentGrowthModuleType.AWARD_SUPPORT, mapOf(
                    "assessmentAcademicYear", "2024-2025",
                    "awardName", "国家奖学金",
                    "batchName", "第一批",
                    "awardLevel", "国家级",
                    "awardGrade", "一等奖",
                    "awardAmount", "8000",
                    "awardType", "奖学金"
            ), LocalDateTime.of(2026, 5, 1, 10, 0)),
            new MockGrowthRecord(102L, 10001L, StudentGrowthModuleType.COMPETITION, mapOf(
                    "awardDate", "2025-11-18",
                    "competitionName", "全国大学生数学建模竞赛",
                    "competitionLevel", "国家级",
                    "competitionGrade", "二等奖",
                    "competitionCategory", "学科竞赛",
                    "organizer", "教育部高教司",
                    "advisorTeacherInfo", "李老师",
                    "remarks", "团队核心成员"
            ), LocalDateTime.of(2026, 5, 2, 9, 30)),
            new MockGrowthRecord(103L, 10001L, StudentGrowthModuleType.INNOVATION_ENTREPRENEURSHIP, mapOf(
                    "startDate", "2025-03-01",
                    "endDate", "2025-12-20",
                    "projectCode", "CX2025-01",
                    "projectName", "校园服务智能问答平台",
                    "collegeName", "信息学院",
                    "projectStatus", "已结项",
                    "projectLevel", "校级",
                    "completionGrade", "优秀",
                    "participantRole", "负责人",
                    "projectType", "创新训练",
                    "projectBatch", "2025年度",
                    "participantCount", "5",
                    "advisorTeacher", "胡老师"
            ), LocalDateTime.of(2026, 5, 3, 14, 20)),
            new MockGrowthRecord(104L, 10001L, StudentGrowthModuleType.SOCIAL_PRACTICE, mapOf(
                    "practiceStartDate", "2025-07-10",
                    "practiceEndDate", "2025-07-18",
                    "practiceTeamName", "乡村振兴实践团",
                    "practiceTheme", "数字助农",
                    "practiceLocation", "河北保定",
                    "practiceTeamLevel", "校级",
                    "advisorTeacher", "王老师"
            ), LocalDateTime.of(2026, 5, 4, 11, 15)),
            new MockGrowthRecord(105L, 10001L, StudentGrowthModuleType.STUDENT_WORK, mapOf(
                    "startDate", "2024-09-01",
                    "endDate", "2025-06-30",
                    "organizationName", "学生会",
                    "positionName", "学习部部长",
                    "workDescription", "负责活动策划与执行"
            ), LocalDateTime.of(2026, 5, 5, 8, 50)),
            new MockGrowthRecord(106L, 10001L, StudentGrowthModuleType.VOLUNTEER_SERVICE, mapOf(
                    "serviceDate", "2025-12-05",
                    "serviceProject", "迎新志愿服务",
                    "serviceLocation", "通州校区",
                    "serviceDurationHours", "8",
                    "serviceOrganizationName", "青年志愿者协会"
            ), LocalDateTime.of(2026, 5, 6, 16, 40)),
            new MockGrowthRecord(107L, 10001L, StudentGrowthModuleType.SKILL_CERTIFICATE, mapOf(
                    "certificateName", "英语六级证书",
                    "obtainedDate", "2025-02-20",
                    "certificateLevel", "国家级",
                    "description", "成绩 580 分"
            ), LocalDateTime.of(2026, 5, 7, 13, 10))
    ));

    @Override
    public List<StudentGrowthModuleResponse> listModules() {
        return Arrays.stream(StudentGrowthModuleType.values())
                .map(module -> new StudentGrowthModuleResponse(module.code(), module.moduleName(), module.editMode(), module.editModeLabel()))
                .toList();
    }

    @Override
    public StudentGrowthArchiveResponse currentArchive() {
        return archiveByStudentId(requireCurrentStudentId());
    }

    @Override
    public StudentGrowthArchiveResponse archiveByStudentId(Long studentId) {
        return new StudentGrowthArchiveResponse(
                studentProfileService.getStudent(studentId),
                Arrays.stream(StudentGrowthModuleType.values())
                        .map(module -> new StudentGrowthModuleArchiveSectionResponse(
                                module.code(),
                                module.moduleName(),
                                module.editMode(),
                                module.editModeLabel(),
                                listRecordsByModule(module, studentId)
                        ))
                        .toList()
        );
    }

    public void importAwardSupportRecord(Long studentId, Map<String, Object> fields) {
        Map<String, String> rawFields = normalizeAndValidate(StudentGrowthModuleType.AWARD_SUPPORT, fields);
        MockGrowthRecord existing = records.stream()
                .filter(item -> item.studentId.equals(studentId) && item.module == StudentGrowthModuleType.AWARD_SUPPORT)
                .filter(item -> matchesAwardSupport(item.rawFields, rawFields))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.rawFields.clear();
            existing.rawFields.putAll(rawFields);
            existing.updatedAt = LocalDateTime.now();
            return;
        }
        records.add(0, new MockGrowthRecord(
                idGenerator.incrementAndGet(),
                studentId,
                StudentGrowthModuleType.AWARD_SUPPORT,
                rawFields,
                LocalDateTime.now()
        ));
    }

    private boolean matchesAwardSupport(Map<String, String> left, Map<String, String> right) {
        return equalsText(left.get("assessmentAcademicYear"), right.get("assessmentAcademicYear"))
                && equalsText(left.get("awardName"), right.get("awardName"))
                && equalsText(left.get("batchName"), right.get("batchName"))
                && equalsText(left.get("awardType"), right.get("awardType"));
    }

    private boolean equalsText(String left, String right) {
        String normalizedLeft = left == null ? "" : left.trim();
        String normalizedRight = right == null ? "" : right.trim();
        return normalizedLeft.equals(normalizedRight);
    }

    @Override
    public List<StudentGrowthRecordResponse> listRecords(String moduleCode) {
        return listRecordsByModule(resolveModule(moduleCode), requireCurrentStudentId());
    }

    @Override
    public StudentGrowthRecordResponse getRecord(String moduleCode, Long id) {
        return toResponse(findRecord(resolveModule(moduleCode), requireCurrentStudentId(), id));
    }

    @Override
    public StudentGrowthRecordResponse createRecord(String moduleCode, StudentGrowthRecordRequest request) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        Map<String, String> rawFields = normalizeAndValidate(module, request == null ? null : request.fields());
        MockGrowthRecord created = new MockGrowthRecord(idGenerator.incrementAndGet(), requireCurrentStudentId(), module, rawFields, LocalDateTime.now());
        records.add(0, created);
        return toResponse(created);
    }

    @Override
    public StudentGrowthRecordResponse updateRecord(String moduleCode, Long id, StudentGrowthRecordRequest request) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        MockGrowthRecord existing = findRecord(module, requireCurrentStudentId(), id);
        existing.rawFields.clear();
        existing.rawFields.putAll(normalizeAndValidate(module, request == null ? null : request.fields()));
        existing.updatedAt = LocalDateTime.now();
        return toResponse(existing);
    }

    @Override
    public void deleteRecord(String moduleCode, Long id) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        records.remove(findRecord(module, requireCurrentStudentId(), id));
    }

    private List<StudentGrowthRecordResponse> listRecordsByModule(StudentGrowthModuleType module, Long studentId) {
        return records.stream()
                .filter(item -> item.studentId.equals(studentId) && item.module == module)
                .sorted((left, right) -> right.updatedAt.compareTo(left.updatedAt))
                .map(this::toResponse)
                .toList();
    }

    private MockGrowthRecord findRecord(StudentGrowthModuleType module, Long studentId, Long id) {
        return records.stream()
                .filter(item -> item.module == module && item.studentId.equals(studentId) && item.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("记录不存在"));
    }

    private StudentGrowthModuleType resolveModule(String moduleCode) {
        try {
            return StudentGrowthModuleType.fromCode(moduleCode);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }
    }

    private void ensureEditable(StudentGrowthModuleType module) {
        if (!module.editable()) {
            throw new BusinessException(module.moduleName() + "当前为禁改数据，如需修改请联系相关部门咨询");
        }
    }

    private Long requireCurrentStudentId() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.studentId() == null) {
            throw new BusinessException("当前账号未关联学生档案");
        }
        return user.studentId();
    }

    private Map<String, String> normalizeAndValidate(StudentGrowthModuleType module, Map<String, Object> fields) {
        Map<String, Object> safeFields = fields == null ? Map.of() : fields;
        Map<String, String> normalized = new LinkedHashMap<>();
        for (StudentGrowthModuleType.FieldDefinition definition : module.fieldDefinitions()) {
            String value = safeFields.get(definition.key()) == null ? "" : String.valueOf(safeFields.get(definition.key())).trim();
            if (definition.required() && value.isBlank()) {
                throw new BusinessException(definition.label() + "不能为空");
            }
            normalized.put(definition.key(), value);
        }
        validateDates(module, normalized);
        validateNumber(normalized, "awardAmount", "奖学金额（元）");
        validateNumber(normalized, "serviceDurationHours", "志愿服务时长");
        validateInteger(normalized, "participantCount", "参与学生总人数");
        return normalized;
    }

    private void validateDates(StudentGrowthModuleType module, Map<String, String> fields) {
        switch (module) {
            case COMPETITION -> parseDate(fields.get("awardDate"), "获奖日期");
            case INNOVATION_ENTREPRENEURSHIP -> validateDateRange(fields.get("startDate"), fields.get("endDate"), "开始日期", "结束日期");
            case SOCIAL_PRACTICE -> validateDateRange(fields.get("practiceStartDate"), fields.get("practiceEndDate"), "实践开始日期", "实践结束日期");
            case STUDENT_WORK -> validateDateRange(fields.get("startDate"), fields.get("endDate"), "开始日期", "结束日期");
            case VOLUNTEER_SERVICE -> parseDate(fields.get("serviceDate"), "志愿服务日期");
            case SKILL_CERTIFICATE -> parseDate(fields.get("obtainedDate"), "获得时间");
            default -> {
            }
        }
    }

    private void validateDateRange(String startValue, String endValue, String startLabel, String endLabel) {
        LocalDate start = parseDate(startValue, startLabel);
        LocalDate end = parseDate(endValue, endLabel);
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(endLabel + "不能早于" + startLabel);
        }
    }

    private LocalDate parseDate(String value, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private void validateNumber(Map<String, String> fields, String key, String label) {
        String value = fields.get(key);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            new BigDecimal(value);
        } catch (Exception ex) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private void validateInteger(Map<String, String> fields, String key, String label) {
        String value = fields.get(key);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            throw new BusinessException(label + "格式不正确");
        }
    }

    private StudentGrowthRecordResponse toResponse(MockGrowthRecord record) {
        List<StudentGrowthFieldItemResponse> fields = record.module.fieldDefinitions().stream()
                .map(definition -> new StudentGrowthFieldItemResponse(
                        definition.key(),
                        definition.label(),
                        record.rawFields.getOrDefault(definition.key(), "")
                ))
                .filter(item -> item.value() != null && !item.value().isBlank())
                .toList();
        return new StudentGrowthRecordResponse(
                record.id,
                record.module.code(),
                record.module.moduleName(),
                record.module.editMode(),
                record.module.editModeLabel(),
                buildSummary(record.module, record.rawFields),
                DATE_TIME_FORMATTER.format(record.updatedAt),
                new LinkedHashMap<>(record.rawFields),
                fields
        );
    }

    private String buildSummary(StudentGrowthModuleType module, Map<String, String> rawFields) {
        return switch (module) {
            case AWARD_SUPPORT -> joinSummary(rawFields.get("assessmentAcademicYear"), rawFields.get("awardName"));
            case COMPETITION -> joinSummary(rawFields.get("competitionName"), rawFields.get("competitionGrade"));
            case INNOVATION_ENTREPRENEURSHIP -> joinSummary(rawFields.get("projectName"), rawFields.get("projectStatus"));
            case SOCIAL_PRACTICE -> joinSummary(rawFields.get("practiceTeamName"), rawFields.get("practiceTheme"));
            case STUDENT_WORK -> joinSummary(rawFields.get("organizationName"), rawFields.get("positionName"));
            case VOLUNTEER_SERVICE -> joinSummary(rawFields.get("serviceProject"), rawFields.get("serviceDurationHours").isBlank() ? null : rawFields.get("serviceDurationHours") + "小时");
            case SKILL_CERTIFICATE -> joinSummary(rawFields.get("certificateName"), rawFields.get("certificateLevel"));
        };
    }

    private String joinSummary(String left, String right) {
        if ((left == null || left.isBlank()) && (right == null || right.isBlank())) {
            return "未命名记录";
        }
        if (left == null || left.isBlank()) {
            return right;
        }
        if (right == null || right.isBlank()) {
            return left;
        }
        return left + " / " + right;
    }

    private static Map<String, String> mapOf(String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            result.put(values[i], values[i + 1]);
        }
        return result;
    }

    private static final class MockGrowthRecord {
        private final Long id;
        private final Long studentId;
        private final StudentGrowthModuleType module;
        private final Map<String, String> rawFields;
        private LocalDateTime updatedAt;

        private MockGrowthRecord(Long id,
                                 Long studentId,
                                 StudentGrowthModuleType module,
                                 Map<String, String> rawFields,
                                 LocalDateTime updatedAt) {
            this.id = id;
            this.studentId = studentId;
            this.module = module;
            this.rawFields = new LinkedHashMap<>(rawFields);
            this.updatedAt = updatedAt;
        }
    }
}
