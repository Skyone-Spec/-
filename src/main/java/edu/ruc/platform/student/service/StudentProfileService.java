package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.domain.StudentPortrait;
import edu.ruc.platform.student.domain.StudentStatusHistory;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitCareerStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitFilterRequest;
import edu.ruc.platform.student.dto.StudentPortraitGpaBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitPageItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitRankBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentPortraitStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitUpsertRequest;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryCreateRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryResponse;
import edu.ruc.platform.student.repository.StudentPortraitRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import edu.ruc.platform.student.repository.StudentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!mock & !kingbase")
@RequiredArgsConstructor
public class StudentProfileService implements StudentProfileApplicationService {

    private final StudentProfileRepository studentProfileRepository;
    private final StudentPortraitRepository studentPortraitRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;

    @Override
    public List<StudentProfileResponse> listStudents() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return studentProfileRepository.findAll().stream()
                .filter(item -> canAccessStudent(user, item))
                .map(item -> toResponse(item, user))
                .toList();
    }

    @Override
    public List<StudentProfileResponse> listStudentsByScope(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return studentProfileRepository.findAll().stream()
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.getGrade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.getClassName()))
                .filter(item -> canAccessStudent(user, item))
                .map(item -> toResponse(item, user))
                .toList();
    }

    @Override
    public StudentProfileResponse getStudent(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return studentProfileRepository.findById(id)
                .filter(item -> canAccessStudent(user, item))
                .map(item -> toResponse(item, user))
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
    }

    @Override
    public StudentProfileResponse currentStudentProfile() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        if (user.studentId() == null) {
            throw new BusinessException("当前账号未关联学生档案");
        }
        return studentProfileRepository.findById(user.studentId())
                .map(item -> toResponse(item, user))
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
        StudentProfile entity = new StudentProfile();
        populateEntity(entity, request);
        StudentProfile saved = studentProfileRepository.save(entity);
        appendStatusHistory(saved.getId(), null, saved.getStatus(), saved.getMajorChangedTo(), "新建学生档案");
        return toResponse(saved, currentUserService.requireCurrentUser());
    }

    @Override
    public StudentProfileResponse updateStudent(Long id, StudentProfileUpsertRequest request) {
        validateStudentRequest(request);
        StudentProfile entity = studentProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        String originalStatus = entity.getStatus();
        String originalChangedMajor = entity.getMajorChangedTo();
        populateEntity(entity, request);
        StudentProfile saved = studentProfileRepository.save(entity);
        recordStatusHistoryIfNeeded(saved.getId(), originalStatus, saved.getStatus(), originalChangedMajor, saved.getMajorChangedTo(), "更新学生档案");
        return toResponse(saved, currentUserService.requireCurrentUser());
    }

    @Override
    public List<StudentStatusHistoryResponse> listStatusHistory(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, profile)) {
            throw new BusinessException("学生不存在或无权访问");
        }
        return studentStatusHistoryRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toStatusHistoryResponse)
                .toList();
    }

    @Override
    public StudentStatusHistoryResponse createStatusHistory(Long studentId, StudentStatusHistoryCreateRequest request) {
        validateStatus(request.status(), request.changedToMajor());
        StudentProfile entity = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        String fromStatus = entity.getStatus();
        entity.setStatus(request.status());
        entity.setMajorChangedTo(request.changedToMajor());
        studentProfileRepository.save(entity);
        return appendStatusHistory(studentId, fromStatus, request.status(), request.changedToMajor(), request.reason());
    }

    @Override
    public StudentPortraitResponse getPortrait(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, profile)
                && !"LEAGUE_SECRETARY".equals(user.role())
                && !("STUDENT".equals(user.role()) && studentId.equals(user.studentId()))) {
            throw new BusinessException("学生不存在或无权访问");
        }
        return studentPortraitRepository.findByStudentId(studentId)
                .map(item -> enforcePortraitAccess(user, profile, item))
                .map(this::toPortraitResponse)
                .orElse(new StudentPortraitResponse(studentId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false));
    }

    @Override
    public StudentPortraitResponse upsertPortrait(Long studentId, StudentPortraitUpsertRequest request) {
        validatePortraitRequest(request);
        StudentPortrait portrait = studentPortraitRepository.findByStudentId(studentId).orElseGet(StudentPortrait::new);
        portrait.setStudentId(studentId);
        portrait.setGender(request.gender());
        portrait.setEthnicity(request.ethnicity());
        portrait.setHonors(request.honors());
        portrait.setScholarships(request.scholarships());
        portrait.setCompetitions(request.competitions());
        portrait.setSocialPractice(request.socialPractice());
        portrait.setVolunteerService(request.volunteerService());
        portrait.setResearchExperience(request.researchExperience());
        portrait.setDisciplineRecords(request.disciplineRecords());
        portrait.setDailyPerformance(request.dailyPerformance());
        portrait.setGpa(request.gpa());
        portrait.setGradeRank(request.gradeRank());
        portrait.setMajorRank(request.majorRank());
        portrait.setCreditsEarned(request.creditsEarned());
        portrait.setCareerOrientation(request.careerOrientation());
        portrait.setRemarks(request.remarks());
        portrait.setUpdatedBy(request.updatedBy());
        portrait.setDataSource(request.dataSource());
        portrait.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        return toPortraitResponse(studentPortraitRepository.save(portrait));
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

    private void populateEntity(StudentProfile entity, StudentProfileUpsertRequest request) {
        entity.setStudentNo(request.studentNo());
        entity.setName(request.name());
        entity.setMajor(request.major());
        entity.setGrade(request.grade());
        entity.setClassName(request.className());
        entity.setAdvisorScope(request.advisorScope());
        entity.setDegreeLevel(request.degreeLevel());
        entity.setEmail(request.email());
        entity.setGraduated(request.graduated());
        entity.setStatus(request.status());
        entity.setMajorChangedTo(request.majorChangedTo());
        entity.setEncryptedIdCardNo(request.encryptedIdCardNo());
        entity.setEncryptedPhone(request.encryptedPhone());
        entity.setEncryptedNativePlace(request.encryptedNativePlace());
        entity.setEncryptedHouseholdAddress(request.encryptedHouseholdAddress());
        entity.setEncryptedSupervisor(request.encryptedSupervisor());
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
        if ((status.equals("TRANSFERRED") || status.equals("WITHDRAWN"))
                && (changedToMajor == null || changedToMajor.isBlank())) {
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
            if ("SUPER_ADMIN".equals(currentUser.role())
                    || "COLLEGE_ADMIN".equals(currentUser.role())
                    || "COUNSELOR".equals(currentUser.role())) {
                throw new BusinessException("维护人不能为空");
            }
        }
    }

    private void validateEncryptedSensitiveField(String fieldName, String value, String plainTextPattern, String label) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (value.matches(plainTextPattern)) {
            throw new BusinessException(fieldName + " 不能直接提交明文" + label + "，请传入加密后内容");
        }
    }

    private StudentProfileResponse toResponse(StudentProfile entity, AuthenticatedUser user) {
        StudentProfileResponse response = new StudentProfileResponse(
                entity.getId(),
                entity.getStudentNo(),
                entity.getName(),
                entity.getMajor(),
                entity.getGrade(),
                entity.getClassName(),
                entity.getAdvisorScope(),
                entity.getDegreeLevel(),
                entity.getEmail(),
                entity.getGraduated(),
                entity.getStatus(),
                entity.getMajorChangedTo(),
                mask(entity.getEncryptedIdCardNo()),
                mask(entity.getEncryptedPhone()),
                mask(entity.getEncryptedNativePlace()),
                mask(entity.getEncryptedHouseholdAddress()),
                mask(entity.getEncryptedSupervisor())
        );
        return maskForRole(response, user);
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "*".repeat(value.length() - 2) + value.substring(value.length() - 2);
    }

    private void recordStatusHistoryIfNeeded(Long studentId,
                                             String originalStatus,
                                             String currentStatus,
                                             String originalChangedMajor,
                                             String currentChangedMajor,
                                             String reason) {
        boolean statusChanged = !java.util.Objects.equals(originalStatus, currentStatus);
        boolean majorChanged = !java.util.Objects.equals(originalChangedMajor, currentChangedMajor);
        if (!statusChanged && !majorChanged) {
            return;
        }
        appendStatusHistory(studentId, originalStatus, currentStatus, currentChangedMajor, reason);
    }

    private StudentStatusHistoryResponse appendStatusHistory(Long studentId,
                                                             String fromStatus,
                                                             String toStatus,
                                                             String changedToMajor,
                                                             String reason) {
        if (toStatus == null || toStatus.isBlank()) {
            throw new BusinessException("学生状态不能为空");
        }
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudentId(studentId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedToMajor(changedToMajor);
        history.setReason(reason);
        history.setChangedBy(user.name());
        history.setChangedByRole(user.role());
        return toStatusHistoryResponse(studentStatusHistoryRepository.save(history));
    }

    private StudentStatusHistoryResponse toStatusHistoryResponse(StudentStatusHistory history) {
        return new StudentStatusHistoryResponse(
                history.getId(),
                history.getStudentId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getChangedToMajor(),
                history.getReason(),
                history.getChangedBy(),
                history.getChangedByRole(),
                history.getCreatedAt()
        );
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
        if ("STUDENT".equals(role) && item.id().equals(user.studentId())) {
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

    private StudentPortraitResponse toPortraitResponse(StudentPortrait portrait) {
        return new StudentPortraitResponse(
                portrait.getStudentId(),
                portrait.getGender(),
                portrait.getEthnicity(),
                portrait.getHonors(),
                portrait.getScholarships(),
                portrait.getCompetitions(),
                portrait.getSocialPractice(),
                portrait.getVolunteerService(),
                portrait.getResearchExperience(),
                portrait.getDisciplineRecords(),
                portrait.getDailyPerformance(),
                portrait.getGpa(),
                portrait.getGradeRank(),
                portrait.getMajorRank(),
                portrait.getCreditsEarned(),
                portrait.getCareerOrientation(),
                portrait.getRemarks(),
                portrait.getUpdatedBy(),
                portrait.getDataSource(),
                portrait.getPublicVisible()
        );
    }

    private List<StudentPortraitPageItemResponse> filterPortraits(StudentPortraitFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedGrade = QueryFilterSupport.trimToNull(request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request.className());
        String normalizedCareerOrientation = QueryFilterSupport.trimToNull(request.careerOrientation());
        java.util.Map<Long, StudentPortrait> portraitMap = studentPortraitRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(StudentPortrait::getStudentId, java.util.function.Function.identity()));
        return studentProfileRepository.findAll().stream()
                .filter(profile -> canAccessStudent(user, profile))
                .map(profile -> toPortraitPageItem(profile, portraitMap.get(profile.getId())))
                .filter(item -> item != null)
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> request.publicVisible() == null || request.publicVisible().equals(item.publicVisible()))
                .filter(item -> normalizedCareerOrientation == null || normalizedCareerOrientation.equals(item.careerOrientation()))
                .filter(item -> request.minGpa() == null || (item.gpa() != null && item.gpa() >= request.minGpa()))
                .toList();
    }

    private StudentPortraitPageItemResponse toPortraitPageItem(StudentProfile profile, StudentPortrait portrait) {
        if (portrait == null) {
            return null;
        }
        return new StudentPortraitPageItemResponse(
                profile.getId(),
                profile.getStudentNo(),
                profile.getName(),
                profile.getGrade(),
                profile.getClassName(),
                portrait.getGpa(),
                portrait.getGradeRank(),
                portrait.getMajorRank(),
                portrait.getCreditsEarned(),
                portrait.getCareerOrientation(),
                portrait.getPublicVisible(),
                portrait.getUpdatedBy()
        );
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

    private List<StudentProfileResponse> filterStudents(StudentProfileFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        validateStudentFilterRequest(request);
        String normalizedGrade = QueryFilterSupport.trimToNull(request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request.className());
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request.status());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request.keyword());
        return studentProfileRepository.findAll().stream()
                .filter(item -> canAccessStudent(user, item))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.getGrade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.getClassName()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.getStatus()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getName(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getStudentNo(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getMajor(), normalizedKeyword))
                .map(item -> toResponse(item, user))
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

    private StudentPortrait enforcePortraitAccess(AuthenticatedUser user, StudentProfile profile, StudentPortrait portrait) {
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return portrait;
        }
        if ("CLASS_ADVISOR".equals(user.role()) && canAccessStudent(user, profile)) {
            return portrait;
        }
        if ("STUDENT".equals(user.role()) && profile.getId().equals(user.studentId())) {
            return portrait;
        }
        if ("LEAGUE_SECRETARY".equals(user.role()) && Boolean.TRUE.equals(portrait.getPublicVisible())) {
            return portrait;
        }
        throw new BusinessException("当前账号无权查看该学生画像");
    }

    private boolean canAccessStudent(AuthenticatedUser user, StudentProfile item) {
        return studentDataScopeService.canAccessStudent(user, item);
    }
}
