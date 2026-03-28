package edu.ruc.platform.student.service;

import edu.ruc.platform.auth.domain.LatestStudentExt;
import edu.ruc.platform.auth.domain.LatestUser;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.repository.LatestStudentExtRepository;
import edu.ruc.platform.auth.repository.LatestUserRepository;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.student.domain.StudentPortrait;
import edu.ruc.platform.student.domain.StudentStatusHistory;
import edu.ruc.platform.student.dto.StudentPortraitCareerStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitFilterRequest;
import edu.ruc.platform.student.dto.StudentPortraitGpaBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitPageItemResponse;
import edu.ruc.platform.student.dto.StudentPortraitRankBandStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitResponse;
import edu.ruc.platform.student.dto.StudentPortraitStatsResponse;
import edu.ruc.platform.student.dto.StudentPortraitUpsertRequest;
import edu.ruc.platform.student.dto.StudentProfileFilterRequest;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.dto.StudentProfileStatsResponse;
import edu.ruc.platform.student.dto.StudentProfileUpsertRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryCreateRequest;
import edu.ruc.platform.student.dto.StudentStatusHistoryResponse;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentPortraitRepository;
import edu.ruc.platform.student.repository.StudentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Profile("kingbase")
@RequiredArgsConstructor
public class KingbaseStudentProfileService implements StudentProfileApplicationService {

    private final LatestUserRepository latestUserRepository;
    private final LatestStudentExtRepository latestStudentExtRepository;
    private final StudentPortraitRepository studentPortraitRepository;
    private final AdvisorScopeBindingRepository advisorScopeBindingRepository;
    private final StudentStatusHistoryRepository studentStatusHistoryRepository;
    private final CurrentUserService currentUserService;
    private final StudentDataScopeService studentDataScopeService;

    @Override
    public List<StudentProfileResponse> listStudents() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return loadAllProfiles().stream()
                .filter(item -> canAccessStudent(user, item))
                .map(item -> toResponse(item, user))
                .toList();
    }

    @Override
    public List<StudentProfileResponse> listStudentsByScope(String grade, String className) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedGrade = QueryFilterSupport.trimToNull(grade);
        String normalizedClassName = QueryFilterSupport.trimToNull(className);
        return loadAllProfiles().stream()
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> canAccessStudent(user, item))
                .map(item -> toResponse(item, user))
                .toList();
    }

    @Override
    public StudentProfileResponse getStudent(Long id) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfileResponse response = buildProfile(id)
                .orElseThrow(() -> new BusinessException("学生不存在或无权访问"));
        if (!canAccessStudent(user, response)) {
            throw new BusinessException("学生不存在或无权访问");
        }
        return toResponse(response, user);
    }

    @Override
    public StudentProfileResponse currentStudentProfile() {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        return buildProfile(user.userId())
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
        LatestUser user = new LatestUser();
        user.setStudentNo(request.studentNo());
        user.setFullName(request.name());
        user.setStatus(toLatestStatus(request.status()));
        user.setIsDeleted(0);
        user = latestUserRepository.save(user);

        LatestStudentExt ext = new LatestStudentExt();
        ext.setStudentNo(request.studentNo());
        populateExt(ext, request);
        latestStudentExtRepository.save(ext);
        appendStatusHistory(user.getId(), null, request.status(), request.majorChangedTo(), "新建学生档案");
        return getStudent(user.getId());
    }

    @Override
    public StudentProfileResponse updateStudent(Long id, StudentProfileUpsertRequest request) {
        validateStudentRequest(request);
        LatestUser user = latestUserRepository.findById(id).orElseThrow(() -> new BusinessException("学生不存在"));
        LatestStudentExt ext = latestStudentExtRepository.findByStudentNoAndIsDeleted(user.getStudentNo(), 0).orElseGet(() -> {
            LatestStudentExt created = new LatestStudentExt();
            created.setStudentNo(user.getStudentNo());
            created.setIsDeleted(0);
            return created;
        });
        String originalStatus = toStudentStatus(user, ext);
        user.setStudentNo(request.studentNo());
        user.setFullName(request.name());
        user.setStatus(toLatestStatus(request.status()));
        latestUserRepository.save(user);
        populateExt(ext, request);
        latestStudentExtRepository.save(ext);
        recordStatusHistoryIfNeeded(id, originalStatus, request.status(), null, request.majorChangedTo(), "更新学生档案");
        return getStudent(id);
    }

    @Override
    public List<StudentStatusHistoryResponse> listStatusHistory(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfileResponse profile = getStudent(studentId);
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
        LatestUser user = latestUserRepository.findById(studentId).orElseThrow(() -> new BusinessException("学生不存在"));
        String fromStatus = toStudentStatus(
                user,
                latestStudentExtRepository.findByStudentNoAndIsDeleted(user.getStudentNo(), 0).orElse(null)
        );
        user.setStatus(toLatestStatus(request.status()));
        latestUserRepository.save(user);
        return appendStatusHistory(studentId, fromStatus, request.status(), request.changedToMajor(), request.reason());
    }

    @Override
    public StudentPortraitResponse getPortrait(Long studentId) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        StudentProfileResponse profile = getStudent(studentId);
        if (!canAccessStudent(user, profile)
                && !"LEAGUE_SECRETARY".equals(user.role())
                && !("STUDENT".equals(user.role()) && studentId.equals(user.userId()))) {
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
                .sorted(Comparator.comparing(StudentPortraitCareerStatsResponse::count).reversed())
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
                .sorted(Comparator.comparingInt(item -> gpaBandOrder(item.band())))
                .toList();
        List<StudentPortraitRankBandStatsResponse> gradeRankBandStats = filtered.stream()
                .filter(item -> item.gradeRank() != null)
                .collect(java.util.stream.Collectors.groupingBy(item -> resolveRankBand(item.gradeRank())))
                .entrySet()
                .stream()
                .map(entry -> new StudentPortraitRankBandStatsResponse(entry.getKey(), entry.getValue().size()))
                .sorted(Comparator.comparingInt(item -> rankBandOrder(item.band())))
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

    private List<StudentProfileResponse> loadAllProfiles() {
        return latestUserRepository.findAll().stream()
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .map(item -> buildProfile(item.getId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(StudentProfileResponse::id))
                .toList();
    }

    private java.util.Optional<StudentProfileResponse> buildProfile(Long userId) {
        return latestUserRepository.findById(userId)
                .filter(item -> item.getIsDeleted() != null && item.getIsDeleted() == 0)
                .map(user -> {
                    LatestStudentExt ext = latestStudentExtRepository.findByStudentNoAndIsDeleted(user.getStudentNo(), 0).orElse(null);
                    return new StudentProfileResponse(
                            user.getId(),
                            user.getStudentNo(),
                            user.getFullName(),
                            ext == null ? null : ext.getMajorName(),
                            ext == null || ext.getGradeYear() == null ? null : ext.getGradeYear() + "级",
                            ext == null ? null : ext.getClassName(),
                            parseAdvisorScope(ext),
                            parseDegreeLevel(ext),
                            null,
                            ext != null && ext.getPartyStatus() != null && ext.getPartyStatus().contains("毕业"),
                            toStudentStatus(user, ext),
                            null,
                            mask(ext == null ? null : ext.getIdCardHash()),
                            mask(ext == null ? null : ext.getPhoneHash()),
                            null,
                            mask(ext == null ? null : ext.getHomeAddressHash()),
                            parseAdvisorScope(ext) == null ? null : mask(parseAdvisorScope(ext))
                    );
                });
    }

    private List<StudentProfileResponse> filterStudents(StudentProfileFilterRequest request) {
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        String normalizedStatus = QueryFilterSupport.normalizeUpper(request == null ? null : request.status());
        if (normalizedStatus != null) {
            if (!List.of("ACTIVE", "SUSPENDED", "GRADUATED", "TRANSFERRED", "WITHDRAWN").contains(normalizedStatus)) {
                throw new BusinessException("学生状态不支持: " + normalizedStatus);
            }
        }
        String normalizedGrade = QueryFilterSupport.trimToNull(request == null ? null : request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request == null ? null : request.className());
        String normalizedKeyword = QueryFilterSupport.trimToNull(request == null ? null : request.keyword());
        return loadAllProfiles().stream()
                .filter(item -> canAccessStudent(user, item))
                .filter(item -> normalizedGrade == null || normalizedGrade.equals(item.grade()))
                .filter(item -> normalizedClassName == null || normalizedClassName.equals(item.className()))
                .filter(item -> normalizedStatus == null || normalizedStatus.equals(item.status()))
                .filter(item -> normalizedKeyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.name(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo(), normalizedKeyword)
                        || QueryFilterSupport.containsIgnoreCase(item.major(), normalizedKeyword))
                .map(item -> toResponse(item, user))
                .toList();
    }

    private void populateExt(LatestStudentExt ext, StudentProfileUpsertRequest request) {
        ext.setMajorName(request.major());
        ext.setGradeYear(parseGradeYear(request.grade()));
        ext.setClassName(request.className());
        ext.setPhoneHash(request.encryptedPhone());
        ext.setHomeAddressHash(request.encryptedHouseholdAddress());
        ext.setIdCardHash(request.encryptedIdCardNo());
        ext.setExtJson(buildExtJson(request));
        ext.setIsDeleted(0);
    }

    private Integer parseGradeYear(String grade) {
        if (grade == null || grade.isBlank()) {
            return null;
        }
        String digits = grade.replaceAll("[^0-9]", "");
        if (digits.length() >= 4) {
            return Integer.parseInt(digits.substring(0, 4));
        }
        return null;
    }

    private String buildExtJson(StudentProfileUpsertRequest request) {
        String advisor = request.advisorScope() == null ? "" : request.advisorScope().replace("\"", "");
        String degreeLevel = request.degreeLevel() == null ? "" : request.degreeLevel().replace("\"", "");
        return "{\"advisor\":\"" + advisor + "\",\"degreeLevel\":\"" + degreeLevel + "\"}";
    }

    private String parseAdvisorScope(LatestStudentExt ext) {
        if (ext == null || ext.getExtJson() == null) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"advisor\"\\s*:\\s*\"([^\"]*)\"").matcher(ext.getExtJson());
        return matcher.find() ? matcher.group(1) : null;
    }

    private String parseDegreeLevel(LatestStudentExt ext) {
        if (ext == null || ext.getExtJson() == null) {
            return "本科";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"degreeLevel\"\\s*:\\s*\"([^\"]*)\"").matcher(ext.getExtJson());
        return matcher.find() && !matcher.group(1).isBlank() ? matcher.group(1) : "本科";
    }

    private String toLatestStatus(String studentStatus) {
        return "SUSPENDED".equals(studentStatus) || "WITHDRAWN".equals(studentStatus) ? "disabled" : "active";
    }

    private String toStudentStatus(LatestUser user, LatestStudentExt ext) {
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            return "SUSPENDED";
        }
        if (ext != null && ext.getPartyStatus() != null && ext.getPartyStatus().contains("毕业")) {
            return "GRADUATED";
        }
        return "ACTIVE";
    }

    private boolean canAccessStudent(AuthenticatedUser user, StudentProfileResponse item) {
        return studentDataScopeService.canAccessStudent(user, item.id());
    }

    private StudentProfileResponse toResponse(StudentProfileResponse response, AuthenticatedUser user) {
        if (response == null) {
            return null;
        }
        if ("SUPER_ADMIN".equals(user.role()) || "COLLEGE_ADMIN".equals(user.role()) || "COUNSELOR".equals(user.role())) {
            return response;
        }
        return new StudentProfileResponse(
                response.id(),
                response.studentNo(),
                response.name(),
                response.major(),
                response.grade(),
                response.className(),
                response.advisorScope(),
                response.degreeLevel(),
                response.email(),
                response.graduated(),
                response.status(),
                response.majorChangedTo(),
                response.maskedIdCardNo(),
                response.maskedPhone(),
                response.maskedNativePlace(),
                response.maskedHouseholdAddress(),
                response.maskedSupervisor()
        );
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private void validateStudentRequest(StudentProfileUpsertRequest request) {
        validateStatus(request.status(), request.majorChangedTo());
        if (Boolean.TRUE.equals(request.graduated()) && !"GRADUATED".equals(request.status())) {
            throw new BusinessException("毕业状态为 true 时学生状态必须为 GRADUATED");
        }
        if (Boolean.FALSE.equals(request.graduated()) && "GRADUATED".equals(request.status())) {
            throw new BusinessException("学生状态为 GRADUATED 时毕业状态必须为 true");
        }
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
    }

    private StudentStatusHistoryResponse appendStatusHistory(Long studentId,
                                                            String fromStatus,
                                                            String toStatus,
                                                            String changedToMajor,
                                                            String reason) {
        AuthenticatedUser currentUser = currentUserService.requireCurrentUser();
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudentId(studentId);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedToMajor(changedToMajor);
        history.setReason(reason);
        history.setChangedBy(currentUser.name());
        history.setChangedByRole(currentUser.role());
        return toStatusHistoryResponse(studentStatusHistoryRepository.save(history));
    }

    private void recordStatusHistoryIfNeeded(Long studentId,
                                            String fromStatus,
                                            String toStatus,
                                            String originalChangedMajor,
                                            String changedToMajor,
                                            String reason) {
        if (!java.util.Objects.equals(fromStatus, toStatus) || !java.util.Objects.equals(originalChangedMajor, changedToMajor)) {
            appendStatusHistory(studentId, fromStatus, toStatus, changedToMajor, reason);
        }
    }

    private StudentStatusHistoryResponse toStatusHistoryResponse(StudentStatusHistory entity) {
        return new StudentStatusHistoryResponse(
                entity.getId(),
                entity.getStudentId(),
                entity.getFromStatus(),
                entity.getToStatus(),
                entity.getChangedToMajor(),
                entity.getReason(),
                entity.getChangedBy(),
                entity.getChangedByRole(),
                entity.getCreatedAt()
        );
    }

    private StudentPortrait enforcePortraitAccess(AuthenticatedUser user,
                                                  StudentProfileResponse profile,
                                                  StudentPortrait portrait) {
        if (!Boolean.TRUE.equals(portrait.getPublicVisible())
                && !("SUPER_ADMIN".equals(user.role())
                || "COLLEGE_ADMIN".equals(user.role())
                || "COUNSELOR".equals(user.role())
                || ("STUDENT".equals(user.role()) && profile.id().equals(user.userId())))) {
            throw new BusinessException("当前无权查看该学生画像");
        }
        return portrait;
    }

    private StudentPortraitResponse toPortraitResponse(StudentPortrait item) {
        return new StudentPortraitResponse(
                item.getStudentId(),
                item.getGender(),
                item.getEthnicity(),
                item.getHonors(),
                item.getScholarships(),
                item.getCompetitions(),
                item.getSocialPractice(),
                item.getVolunteerService(),
                item.getResearchExperience(),
                item.getDisciplineRecords(),
                item.getDailyPerformance(),
                item.getGpa(),
                item.getGradeRank(),
                item.getMajorRank(),
                item.getCreditsEarned(),
                item.getCareerOrientation(),
                item.getRemarks(),
                item.getUpdatedBy(),
                item.getDataSource(),
                item.getPublicVisible()
        );
    }

    private List<StudentPortraitPageItemResponse> filterPortraits(StudentPortraitFilterRequest request) {
        String normalizedCareerOrientation = QueryFilterSupport.trimToNull(request == null ? null : request.careerOrientation());
        String normalizedGrade = QueryFilterSupport.trimToNull(request == null ? null : request.grade());
        String normalizedClassName = QueryFilterSupport.trimToNull(request == null ? null : request.className());
        Boolean publicVisible = request == null ? null : request.publicVisible();
        Double minGpa = request == null ? null : request.minGpa();
        List<Long> accessibleStudentIds = listStudents().stream().map(StudentProfileResponse::id).toList();
        return studentPortraitRepository.findAll().stream()
                .filter(item -> accessibleStudentIds.contains(item.getStudentId()))
                .filter(item -> {
                    StudentProfileResponse profile = buildProfile(item.getStudentId()).orElse(null);
                    return normalizedGrade == null || (profile != null && normalizedGrade.equals(profile.grade()));
                })
                .filter(item -> {
                    StudentProfileResponse profile = buildProfile(item.getStudentId()).orElse(null);
                    return normalizedClassName == null || (profile != null && normalizedClassName.equals(profile.className()));
                })
                .filter(item -> publicVisible == null || publicVisible.equals(item.getPublicVisible()))
                .filter(item -> normalizedCareerOrientation == null || normalizedCareerOrientation.equals(item.getCareerOrientation()))
                .filter(item -> minGpa == null || (item.getGpa() != null && item.getGpa() >= minGpa))
                .map(item -> {
                    StudentProfileResponse profile = buildProfile(item.getStudentId()).orElse(null);
                    return new StudentPortraitPageItemResponse(
                            item.getStudentId(),
                            profile == null ? null : profile.studentNo(),
                            profile == null ? "待补充" : profile.name(),
                            profile == null ? null : profile.grade(),
                            profile == null ? null : profile.className(),
                            item.getGpa(),
                            item.getGradeRank(),
                            item.getMajorRank(),
                            item.getCreditsEarned(),
                            item.getCareerOrientation(),
                            item.getPublicVisible(),
                            item.getUpdatedBy()
                    );
                })
                .toList();
    }

    private String resolveGpaBand(Double gpa) {
        if (gpa == null) {
            return "未知";
        }
        if (gpa >= 4.0) {
            return "4.0+";
        }
        if (gpa >= 3.5) {
            return "3.5-3.99";
        }
        if (gpa >= 3.0) {
            return "3.0-3.49";
        }
        return "<3.0";
    }

    private int gpaBandOrder(String band) {
        return switch (band) {
            case "4.0+" -> 1;
            case "3.5-3.99" -> 2;
            case "3.0-3.49" -> 3;
            case "<3.0" -> 4;
            default -> 5;
        };
    }

    private String resolveRankBand(Integer rank) {
        if (rank == null) {
            return "未知";
        }
        if (rank <= 10) {
            return "前10";
        }
        if (rank <= 30) {
            return "11-30";
        }
        if (rank <= 50) {
            return "31-50";
        }
        return "50+";
    }

    private int rankBandOrder(String band) {
        return switch (band) {
            case "前10" -> 1;
            case "11-30" -> 2;
            case "31-50" -> 3;
            case "50+" -> 4;
            default -> 5;
        };
    }
}
