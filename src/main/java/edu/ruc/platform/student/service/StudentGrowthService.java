package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.domain.StudentAwardSupportRecord;
import edu.ruc.platform.student.domain.StudentCompetitionRecord;
import edu.ruc.platform.student.domain.StudentInnovationEntrepreneurshipRecord;
import edu.ruc.platform.student.domain.StudentSkillCertificateRecord;
import edu.ruc.platform.student.domain.StudentSocialPracticeRecord;
import edu.ruc.platform.student.domain.StudentStudentWorkRecord;
import edu.ruc.platform.student.domain.StudentVolunteerServiceRecord;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthArchiveResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthFieldItemResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleArchiveSectionResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthModuleResponse;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordRequest;
import edu.ruc.platform.student.dto.StudentGrowthDtos.StudentGrowthRecordResponse;
import edu.ruc.platform.student.repository.StudentAwardSupportRecordRepository;
import edu.ruc.platform.student.repository.StudentCompetitionRecordRepository;
import edu.ruc.platform.student.repository.StudentInnovationEntrepreneurshipRecordRepository;
import edu.ruc.platform.student.repository.StudentSkillCertificateRecordRepository;
import edu.ruc.platform.student.repository.StudentSocialPracticeRecordRepository;
import edu.ruc.platform.student.repository.StudentStudentWorkRecordRepository;
import edu.ruc.platform.student.repository.StudentVolunteerServiceRecordRepository;
import edu.ruc.platform.student.support.StudentGrowthModuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class StudentGrowthService implements StudentGrowthApplicationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CurrentUserService currentUserService;
    private final StudentProfileApplicationService studentProfileService;
    private final StudentAwardSupportRecordRepository studentAwardSupportRecordRepository;
    private final StudentCompetitionRecordRepository studentCompetitionRecordRepository;
    private final StudentInnovationEntrepreneurshipRecordRepository studentInnovationEntrepreneurshipRecordRepository;
    private final StudentSocialPracticeRecordRepository studentSocialPracticeRecordRepository;
    private final StudentStudentWorkRecordRepository studentStudentWorkRecordRepository;
    private final StudentVolunteerServiceRecordRepository studentVolunteerServiceRecordRepository;
    private final StudentSkillCertificateRecordRepository studentSkillCertificateRecordRepository;

    @Override
    public List<StudentGrowthModuleResponse> listModules() {
        return Arrays.stream(StudentGrowthModuleType.values())
                .map(this::toModuleResponse)
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
                                listRecordResponses(module, studentId)
                        ))
                        .toList()
        );
    }

    @Override
    public List<StudentGrowthRecordResponse> listRecords(String moduleCode) {
        return listRecordResponses(resolveModule(moduleCode), requireCurrentStudentId());
    }

    @Override
    public StudentGrowthRecordResponse getRecord(String moduleCode, Long id) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        return toRecordResponse(module, getOwnedRecord(module, requireCurrentStudentId(), id));
    }

    @Override
    public StudentGrowthRecordResponse createRecord(String moduleCode, StudentGrowthRecordRequest request) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        Object entity = newEntity(module);
        setProperty(entity, "studentId", requireCurrentStudentId());
        populateFields(entity, module, request == null ? null : request.fields());
        validateEntity(entity, module);
        return toRecordResponse(module, saveEntity(module, entity));
    }

    @Override
    public StudentGrowthRecordResponse updateRecord(String moduleCode, Long id, StudentGrowthRecordRequest request) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        Object entity = getOwnedRecord(module, requireCurrentStudentId(), id);
        populateFields(entity, module, request == null ? null : request.fields());
        validateEntity(entity, module);
        return toRecordResponse(module, saveEntity(module, entity));
    }

    @Override
    public void deleteRecord(String moduleCode, Long id) {
        StudentGrowthModuleType module = resolveModule(moduleCode);
        ensureEditable(module);
        deleteEntity(module, getOwnedRecord(module, requireCurrentStudentId(), id));
    }

    private StudentGrowthModuleResponse toModuleResponse(StudentGrowthModuleType module) {
        return new StudentGrowthModuleResponse(module.code(), module.moduleName(), module.editMode(), module.editModeLabel());
    }

    private Long requireCurrentStudentId() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.studentId() == null) {
            throw new BusinessException("当前账号未关联学生档案");
        }
        return user.studentId();
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

    private List<StudentGrowthRecordResponse> listRecordResponses(StudentGrowthModuleType module, Long studentId) {
        return listRawRecords(module, studentId).stream()
                .map(item -> toRecordResponse(module, item))
                .toList();
    }

    private List<?> listRawRecords(StudentGrowthModuleType module, Long studentId) {
        return switch (module) {
            case AWARD_SUPPORT -> studentAwardSupportRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case COMPETITION -> studentCompetitionRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case INNOVATION_ENTREPRENEURSHIP -> studentInnovationEntrepreneurshipRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case SOCIAL_PRACTICE -> studentSocialPracticeRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case STUDENT_WORK -> studentStudentWorkRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case VOLUNTEER_SERVICE -> studentVolunteerServiceRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
            case SKILL_CERTIFICATE -> studentSkillCertificateRecordRepository.findByStudentIdOrderByUpdatedAtDescIdDesc(studentId);
        };
    }

    private Object getOwnedRecord(StudentGrowthModuleType module, Long studentId, Long id) {
        return switch (module) {
            case AWARD_SUPPORT -> studentAwardSupportRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case COMPETITION -> studentCompetitionRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case INNOVATION_ENTREPRENEURSHIP -> studentInnovationEntrepreneurshipRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case SOCIAL_PRACTICE -> studentSocialPracticeRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case STUDENT_WORK -> studentStudentWorkRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case VOLUNTEER_SERVICE -> studentVolunteerServiceRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
            case SKILL_CERTIFICATE -> studentSkillCertificateRecordRepository.findByIdAndStudentId(id, studentId).orElseThrow(() -> new BusinessException("记录不存在"));
        };
    }

    private Object newEntity(StudentGrowthModuleType module) {
        return switch (module) {
            case AWARD_SUPPORT -> new StudentAwardSupportRecord();
            case COMPETITION -> new StudentCompetitionRecord();
            case INNOVATION_ENTREPRENEURSHIP -> new StudentInnovationEntrepreneurshipRecord();
            case SOCIAL_PRACTICE -> new StudentSocialPracticeRecord();
            case STUDENT_WORK -> new StudentStudentWorkRecord();
            case VOLUNTEER_SERVICE -> new StudentVolunteerServiceRecord();
            case SKILL_CERTIFICATE -> new StudentSkillCertificateRecord();
        };
    }

    private Object saveEntity(StudentGrowthModuleType module, Object entity) {
        return switch (module) {
            case AWARD_SUPPORT -> studentAwardSupportRecordRepository.save((StudentAwardSupportRecord) entity);
            case COMPETITION -> studentCompetitionRecordRepository.save((StudentCompetitionRecord) entity);
            case INNOVATION_ENTREPRENEURSHIP -> studentInnovationEntrepreneurshipRecordRepository.save((StudentInnovationEntrepreneurshipRecord) entity);
            case SOCIAL_PRACTICE -> studentSocialPracticeRecordRepository.save((StudentSocialPracticeRecord) entity);
            case STUDENT_WORK -> studentStudentWorkRecordRepository.save((StudentStudentWorkRecord) entity);
            case VOLUNTEER_SERVICE -> studentVolunteerServiceRecordRepository.save((StudentVolunteerServiceRecord) entity);
            case SKILL_CERTIFICATE -> studentSkillCertificateRecordRepository.save((StudentSkillCertificateRecord) entity);
        };
    }

    private void deleteEntity(StudentGrowthModuleType module, Object entity) {
        switch (module) {
            case AWARD_SUPPORT -> studentAwardSupportRecordRepository.delete((StudentAwardSupportRecord) entity);
            case COMPETITION -> studentCompetitionRecordRepository.delete((StudentCompetitionRecord) entity);
            case INNOVATION_ENTREPRENEURSHIP -> studentInnovationEntrepreneurshipRecordRepository.delete((StudentInnovationEntrepreneurshipRecord) entity);
            case SOCIAL_PRACTICE -> studentSocialPracticeRecordRepository.delete((StudentSocialPracticeRecord) entity);
            case STUDENT_WORK -> studentStudentWorkRecordRepository.delete((StudentStudentWorkRecord) entity);
            case VOLUNTEER_SERVICE -> studentVolunteerServiceRecordRepository.delete((StudentVolunteerServiceRecord) entity);
            case SKILL_CERTIFICATE -> studentSkillCertificateRecordRepository.delete((StudentSkillCertificateRecord) entity);
        }
    }

    private void populateFields(Object entity, StudentGrowthModuleType module, Map<String, Object> fields) {
        Map<String, Object> safeFields = fields == null ? Map.of() : fields;
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        for (StudentGrowthModuleType.FieldDefinition fieldDefinition : module.fieldDefinitions()) {
            Object rawValue = safeFields.get(fieldDefinition.key());
            if (rawValue == null || String.valueOf(rawValue).trim().isBlank()) {
                if (fieldDefinition.required()) {
                    throw new BusinessException(fieldDefinition.label() + "不能为空");
                }
                wrapper.setPropertyValue(fieldDefinition.key(), null);
                continue;
            }
            Class<?> propertyType = wrapper.getPropertyType(fieldDefinition.key());
            wrapper.setPropertyValue(fieldDefinition.key(), convertValue(propertyType, rawValue, fieldDefinition.label()));
        }
    }

    private Object convertValue(Class<?> propertyType, Object rawValue, String label) {
        String value = String.valueOf(rawValue).trim();
        try {
            if (String.class.equals(propertyType)) {
                return value;
            }
            if (LocalDate.class.equals(propertyType)) {
                return LocalDate.parse(value);
            }
            if (Integer.class.equals(propertyType)) {
                return Integer.valueOf(value);
            }
            if (Long.class.equals(propertyType)) {
                return Long.valueOf(value);
            }
            if (BigDecimal.class.equals(propertyType)) {
                return new BigDecimal(value);
            }
        } catch (Exception ex) {
            throw new BusinessException(label + "格式不正确");
        }
        return rawValue;
    }

    private void setProperty(Object entity, String propertyName, Object value) {
        new BeanWrapperImpl(entity).setPropertyValue(propertyName, value);
    }

    private void validateEntity(Object entity, StudentGrowthModuleType module) {
        switch (module) {
            case INNOVATION_ENTREPRENEURSHIP, STUDENT_WORK -> validateDateRange(entity, "startDate", "endDate", "结束日期不能早于开始日期");
            case SOCIAL_PRACTICE -> validateDateRange(entity, "practiceStartDate", "practiceEndDate", "实践结束日期不能早于实践开始日期");
            default -> {
            }
        }
    }

    private void validateDateRange(Object entity, String startProperty, String endProperty, String message) {
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        LocalDate start = (LocalDate) wrapper.getPropertyValue(startProperty);
        LocalDate end = (LocalDate) wrapper.getPropertyValue(endProperty);
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(message);
        }
    }

    private StudentGrowthRecordResponse toRecordResponse(StudentGrowthModuleType module, Object entity) {
        BeanWrapper wrapper = new BeanWrapperImpl(entity);
        Map<String, String> rawFields = new LinkedHashMap<>();
        List<StudentGrowthFieldItemResponse> fields = module.fieldDefinitions().stream()
                .map(definition -> {
                    String formatted = formatValue(wrapper.getPropertyValue(definition.key()));
                    rawFields.put(definition.key(), formatted);
                    return new StudentGrowthFieldItemResponse(definition.key(), definition.label(), formatted);
                })
                .filter(item -> item.value() != null && !item.value().isBlank())
                .toList();
        return new StudentGrowthRecordResponse(
                (Long) wrapper.getPropertyValue("id"),
                module.code(),
                module.moduleName(),
                module.editMode(),
                module.editModeLabel(),
                buildSummary(module, rawFields),
                formatDateTime((LocalDateTime) wrapper.getPropertyValue("updatedAt")),
                rawFields,
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

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDate localDate) {
            return localDate.toString();
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? "" : DATE_TIME_FORMATTER.format(time);
    }
}
