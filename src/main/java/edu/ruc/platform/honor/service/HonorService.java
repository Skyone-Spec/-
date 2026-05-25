package edu.ruc.platform.honor.service;

import edu.ruc.platform.admin.repository.DataImportTaskRepository;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
import edu.ruc.platform.honor.domain.HonorRecipient;
import edu.ruc.platform.honor.domain.HonorRecipientAttachment;
import edu.ruc.platform.honor.domain.HonorRecipientMember;
import edu.ruc.platform.honor.domain.HonorShowcase;
import edu.ruc.platform.honor.dto.HonorRecipientAdminResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentStudentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientFilterRequest;
import edu.ruc.platform.honor.dto.HonorRecipientMemberResponse;
import edu.ruc.platform.honor.dto.HonorRecipientMemberStudentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientMemberUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientStudentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientUpsertRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseAdminResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseFilterRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseStudentResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseUpsertRequest;
import edu.ruc.platform.honor.repository.HonorRecipientAttachmentRepository;
import edu.ruc.platform.honor.repository.HonorRecipientMemberRepository;
import edu.ruc.platform.honor.repository.HonorRecipientRepository;
import edu.ruc.platform.honor.repository.HonorShowcaseRepository;
import edu.ruc.platform.platform.domain.PlatformFileUploadRecord;
import edu.ruc.platform.platform.repository.PlatformFileUploadRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class HonorService implements HonorApplicationService {

    private static final Set<String> RECIPIENT_TYPES = Set.of("PERSONAL", "COLLECTIVE");
    private static final Set<String> ATTACHMENT_TYPES = Set.of("PHOTO", "DOCUMENT", "VIDEO", "OTHER");

    private final HonorShowcaseRepository showcaseRepository;
    private final HonorRecipientRepository recipientRepository;
    private final HonorRecipientMemberRepository memberRepository;
    private final HonorRecipientAttachmentRepository attachmentRepository;
    private final DataImportTaskRepository dataImportTaskRepository;
    private final PlatformFileUploadRecordRepository uploadRecordRepository;
    private final CurrentUserService currentUserService;

    @Override
    public PageResponse<HonorShowcaseAdminResponse> pageAdminShowcases(HonorShowcaseFilterRequest request, int page, int size) {
        List<HonorShowcaseAdminResponse> filtered = filterShowcases(request).stream()
                .map(item -> toAdminShowcaseResponse(item, false))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public HonorShowcaseAdminResponse getAdminShowcase(Long id) {
        return toAdminShowcaseResponse(requireShowcase(id), true);
    }

    @Override
    @Transactional
    public HonorShowcaseAdminResponse createShowcase(HonorShowcaseUpsertRequest request) {
        validateDisplayTime(request.displayStartAt(), request.displayEndAt());
        validateRecipientType(request.recipientType());
        validateImportTask(request.importTaskId());
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        HonorShowcase showcase = new HonorShowcase();
        populateShowcase(showcase, request);
        showcase.setCreatedById(user.userId());
        showcase.setCreatedByName(user.name());
        showcase.setUpdatedBy(user.name());
        return toAdminShowcaseResponse(showcaseRepository.save(showcase), true);
    }

    @Override
    @Transactional
    public HonorShowcaseAdminResponse updateShowcase(Long id, HonorShowcaseUpsertRequest request) {
        validateDisplayTime(request.displayStartAt(), request.displayEndAt());
        validateRecipientType(request.recipientType());
        validateImportTask(request.importTaskId());
        HonorShowcase showcase = requireShowcase(id);
        populateShowcase(showcase, request);
        showcase.setUpdatedBy(currentUserService.requireCurrentUser().name());
        return toAdminShowcaseResponse(showcaseRepository.save(showcase), true);
    }

    @Override
    @Transactional
    public void deleteShowcase(Long id) {
        HonorShowcase showcase = requireShowcase(id);
        List<HonorRecipient> recipients = recipientRepository.findByShowcaseIdOrderByDisplayOrderAscCreatedAtDesc(showcase.getId());
        for (HonorRecipient recipient : recipients) {
            attachmentRepository.deleteByRecipientId(recipient.getId());
            memberRepository.deleteByRecipientId(recipient.getId());
        }
        recipientRepository.deleteByShowcaseId(showcase.getId());
        showcaseRepository.delete(showcase);
    }

    @Override
    public PageResponse<HonorRecipientAdminResponse> pageAdminRecipients(Long showcaseId, HonorRecipientFilterRequest request, int page, int size) {
        requireShowcase(showcaseId);
        List<HonorRecipientAdminResponse> filtered = filterRecipients(showcaseId, request).stream()
                .map(item -> toAdminRecipientResponse(item, false))
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public HonorRecipientAdminResponse getAdminRecipient(Long recipientId) {
        return toAdminRecipientResponse(requireRecipient(recipientId), true);
    }

    @Override
    @Transactional
    public HonorRecipientAdminResponse createRecipient(Long showcaseId, HonorRecipientUpsertRequest request) {
        HonorShowcase showcase = requireShowcase(showcaseId);
        validateRecipientRequest(request);
        requireMatchingRecipientType(showcase.getRecipientType(), request.recipientType());
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        HonorRecipient recipient = new HonorRecipient();
        recipient.setShowcaseId(showcase.getId());
        populateRecipient(recipient, request);
        recipient.setCreatedById(user.userId());
        recipient.setCreatedByName(user.name());
        recipient.setUpdatedBy(user.name());
        return toAdminRecipientResponse(recipientRepository.save(recipient), true);
    }

    @Override
    @Transactional
    public HonorRecipientAdminResponse updateRecipient(Long recipientId, HonorRecipientUpsertRequest request) {
        validateRecipientRequest(request);
        HonorRecipient recipient = requireRecipient(recipientId);
        requireMatchingRecipientType(requireShowcase(recipient.getShowcaseId()).getRecipientType(), request.recipientType());
        populateRecipient(recipient, request);
        recipient.setUpdatedBy(currentUserService.requireCurrentUser().name());
        return toAdminRecipientResponse(recipientRepository.save(recipient), true);
    }

    @Override
    @Transactional
    public void deleteRecipient(Long recipientId) {
        HonorRecipient recipient = requireRecipient(recipientId);
        attachmentRepository.deleteByRecipientId(recipient.getId());
        memberRepository.deleteByRecipientId(recipient.getId());
        recipientRepository.delete(recipient);
    }

    @Override
    @Transactional
    public HonorRecipientMemberResponse createMember(Long recipientId, HonorRecipientMemberUpsertRequest request) {
        HonorRecipient recipient = requireRecipient(recipientId);
        if (!"COLLECTIVE".equals(recipient.getRecipientType())) {
            throw new BusinessException("仅集体荣誉获得者可维护成员");
        }
        HonorRecipientMember member = new HonorRecipientMember();
        member.setRecipientId(recipientId);
        populateMember(member, request);
        return toMemberResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public HonorRecipientMemberResponse updateMember(Long memberId, HonorRecipientMemberUpsertRequest request) {
        HonorRecipientMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("荣誉成员不存在"));
        populateMember(member, request);
        return toMemberResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        HonorRecipientMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("荣誉成员不存在"));
        memberRepository.delete(member);
    }

    @Override
    @Transactional
    public HonorRecipientAttachmentResponse createAttachment(Long recipientId, HonorRecipientAttachmentUpsertRequest request) {
        requireRecipient(recipientId);
        HonorRecipientAttachment attachment = new HonorRecipientAttachment();
        attachment.setRecipientId(recipientId);
        populateAttachment(attachment, request);
        return toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public HonorRecipientAttachmentResponse updateAttachment(Long attachmentId, HonorRecipientAttachmentUpsertRequest request) {
        HonorRecipientAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("荣誉附件不存在"));
        populateAttachment(attachment, request);
        return toAttachmentResponse(attachmentRepository.save(attachment));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        HonorRecipientAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("荣誉附件不存在"));
        attachmentRepository.delete(attachment);
    }

    @Override
    public PageResponse<HonorShowcaseStudentResponse> pageStudentShowcases(HonorShowcaseFilterRequest request, int page, int size) {
        List<HonorShowcaseStudentResponse> filtered = filterShowcases(request).stream()
                .filter(this::isVisibleNow)
                .map(item -> toStudentShowcaseResponse(item, false))
                .filter(item -> item.recipientCount() > 0)
                .toList();
        return toPage(filtered, page, size);
    }

    @Override
    public HonorShowcaseStudentResponse getStudentShowcase(Long id) {
        HonorShowcase showcase = requireVisibleShowcase(id);
        return toStudentShowcaseResponse(showcase, true);
    }

    @Override
    public List<HonorRecipientStudentResponse> listStudentRecipients(Long showcaseId) {
        HonorShowcase showcase = requireVisibleShowcase(showcaseId);
        return visibleRecipients(showcase.getId()).stream()
                .map(this::toStudentRecipientResponse)
                .toList();
    }

    @Override
    public HonorRecipientStudentResponse getStudentRecipient(Long recipientId) {
        HonorRecipient recipient = requireRecipient(recipientId);
        requireVisibleShowcase(recipient.getShowcaseId());
        if (!isVisibleNow(recipient)) {
            throw new BusinessException("荣誉获得者未公开或不在展示时效内");
        }
        return toStudentRecipientResponse(recipient);
    }

    private List<HonorShowcase> filterShowcases(HonorShowcaseFilterRequest request) {
        HonorShowcaseFilterRequest safe = request == null ? new HonorShowcaseFilterRequest(null, null, null, null, null) : request;
        String category = QueryFilterSupport.trimToNull(safe.honorCategory());
        String recipientType = normalizeRecipientType(safe.recipientType(), false);
        String keyword = QueryFilterSupport.trimToNull(safe.keyword());
        return showcaseRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc().stream()
                .filter(item -> safe.awardYear() == null || safe.awardYear().equals(item.getAwardYear()))
                .filter(item -> category == null || QueryFilterSupport.containsIgnoreCase(item.getHonorCategory(), category))
                .filter(item -> recipientType == null || recipientType.equals(item.getRecipientType()))
                .filter(item -> safe.publicVisible() == null || safe.publicVisible().equals(item.getPublicVisible()))
                .filter(item -> keyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getTitle(), keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getDescription(), keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getHonorCategory(), keyword))
                .toList();
    }

    private List<HonorRecipient> filterRecipients(Long showcaseId, HonorRecipientFilterRequest request) {
        HonorRecipientFilterRequest safe = request == null ? new HonorRecipientFilterRequest(null, null, null) : request;
        String recipientType = normalizeRecipientType(safe.recipientType(), false);
        String keyword = QueryFilterSupport.trimToNull(safe.keyword());
        return recipientRepository.findByShowcaseIdOrderByDisplayOrderAscCreatedAtDesc(showcaseId).stream()
                .filter(item -> recipientType == null || recipientType.equals(item.getRecipientType()))
                .filter(item -> safe.publicVisible() == null || safe.publicVisible().equals(item.getPublicVisible()))
                .filter(item -> keyword == null
                        || QueryFilterSupport.containsIgnoreCase(item.getRecipientName(), keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getStudentNo(), keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getAwardIntro(), keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.getAdvancedDeeds(), keyword))
                .toList();
    }

    private List<HonorRecipient> visibleRecipients(Long showcaseId) {
        return recipientRepository.findByShowcaseIdOrderByDisplayOrderAscCreatedAtDesc(showcaseId).stream()
                .filter(this::isVisibleNow)
                .toList();
    }

    private HonorShowcaseAdminResponse toAdminShowcaseResponse(HonorShowcase showcase, boolean includeRecipients) {
        List<HonorRecipientAdminResponse> recipients = includeRecipients
                ? recipientRepository.findByShowcaseIdOrderByDisplayOrderAscCreatedAtDesc(showcase.getId()).stream()
                .map(item -> toAdminRecipientResponse(item, true))
                .toList()
                : List.of();
        return new HonorShowcaseAdminResponse(
                showcase.getId(),
                showcase.getAwardYear(),
                showcase.getHonorCategory(),
                showcase.getRecipientType(),
                showcase.getTitle(),
                showcase.getDescription(),
                showcase.getPublicVisible(),
                showcase.getDisplayOrder(),
                showcase.getDisplayStartAt(),
                showcase.getDisplayEndAt(),
                showcase.getImportTaskId(),
                showcase.getCreatedByName(),
                showcase.getUpdatedBy(),
                showcase.getCreatedAt(),
                showcase.getUpdatedAt(),
                recipientRepository.countByShowcaseId(showcase.getId()),
                recipients
        );
    }

    private HonorRecipientAdminResponse toAdminRecipientResponse(HonorRecipient recipient, boolean includeChildren) {
        List<HonorRecipientMemberResponse> members = includeChildren
                ? memberRepository.findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(recipient.getId()).stream().map(this::toMemberResponse).toList()
                : List.of();
        List<HonorRecipientAttachmentResponse> attachments = includeChildren
                ? attachmentRepository.findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(recipient.getId()).stream().map(this::toAttachmentResponse).toList()
                : List.of();
        return new HonorRecipientAdminResponse(
                recipient.getId(),
                recipient.getShowcaseId(),
                recipient.getRecipientType(),
                recipient.getStudentId(),
                recipient.getStudentNo(),
                recipient.getRecipientName(),
                recipient.getMajor(),
                recipient.getGrade(),
                recipient.getClassName(),
                recipient.getAwardIntro(),
                recipient.getAdvancedDeeds(),
                recipient.getPhotoFileId(),
                recipient.getPublicVisible(),
                recipient.getDisplayOrder(),
                recipient.getDisplayStartAt(),
                recipient.getDisplayEndAt(),
                recipient.getImportTaskId(),
                recipient.getCreatedByName(),
                recipient.getUpdatedBy(),
                recipient.getCreatedAt(),
                recipient.getUpdatedAt(),
                members,
                attachments
        );
    }

    private HonorShowcaseStudentResponse toStudentShowcaseResponse(HonorShowcase showcase, boolean includeRecipients) {
        List<HonorRecipientStudentResponse> recipients = includeRecipients
                ? visibleRecipients(showcase.getId()).stream().map(this::toStudentRecipientResponse).toList()
                : List.of();
        return new HonorShowcaseStudentResponse(
                showcase.getId(),
                showcase.getAwardYear(),
                showcase.getHonorCategory(),
                showcase.getRecipientType(),
                showcase.getTitle(),
                showcase.getDescription(),
                visibleRecipients(showcase.getId()).size(),
                recipients
        );
    }

    private HonorRecipientStudentResponse toStudentRecipientResponse(HonorRecipient recipient) {
        return new HonorRecipientStudentResponse(
                recipient.getId(),
                recipient.getShowcaseId(),
                recipient.getRecipientType(),
                recipient.getRecipientName(),
                recipient.getMajor(),
                recipient.getGrade(),
                recipient.getClassName(),
                recipient.getAwardIntro(),
                recipient.getAdvancedDeeds(),
                recipient.getPhotoFileId(),
                memberRepository.findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(recipient.getId()).stream()
                        .map(this::toStudentMemberResponse)
                        .toList(),
                attachmentRepository.findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(recipient.getId()).stream()
                        .filter(item -> Boolean.TRUE.equals(item.getPublicVisible()))
                        .map(this::toStudentAttachmentResponse)
                        .toList()
        );
    }

    private HonorRecipientMemberResponse toMemberResponse(HonorRecipientMember member) {
        return new HonorRecipientMemberResponse(member.getId(), member.getRecipientId(), member.getStudentId(), member.getStudentNo(),
                member.getStudentName(), member.getMajor(), member.getGrade(), member.getClassName(), member.getMemberRole(), member.getDisplayOrder());
    }

    private HonorRecipientMemberStudentResponse toStudentMemberResponse(HonorRecipientMember member) {
        return new HonorRecipientMemberStudentResponse(member.getId(), member.getStudentName(), member.getMajor(), member.getGrade(),
                member.getClassName(), member.getMemberRole());
    }

    private HonorRecipientAttachmentResponse toAttachmentResponse(HonorRecipientAttachment attachment) {
        return new HonorRecipientAttachmentResponse(attachment.getId(), attachment.getRecipientId(), attachment.getFileId(), attachment.getAttachmentType(),
                attachment.getFileName(), attachment.getContentType(), attachment.getFileSize(), attachment.getStoragePath(),
                attachment.getCaption(), attachment.getPublicVisible(), attachment.getDisplayOrder());
    }

    private HonorRecipientAttachmentStudentResponse toStudentAttachmentResponse(HonorRecipientAttachment attachment) {
        return new HonorRecipientAttachmentStudentResponse(attachment.getId(), attachment.getFileId(), attachment.getAttachmentType(),
                attachment.getFileName(), attachment.getContentType(), attachment.getFileSize(), attachment.getStoragePath(), attachment.getCaption());
    }

    private void populateShowcase(HonorShowcase showcase, HonorShowcaseUpsertRequest request) {
        showcase.setAwardYear(request.awardYear());
        showcase.setHonorCategory(QueryFilterSupport.trimToNull(request.honorCategory()));
        showcase.setRecipientType(normalizeRecipientType(request.recipientType(), true));
        showcase.setTitle(QueryFilterSupport.trimToNull(request.title()));
        showcase.setDescription(QueryFilterSupport.trimToNull(request.description()));
        showcase.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        showcase.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
        showcase.setDisplayStartAt(request.displayStartAt());
        showcase.setDisplayEndAt(request.displayEndAt());
        showcase.setImportTaskId(request.importTaskId());
    }

    private void populateRecipient(HonorRecipient recipient, HonorRecipientUpsertRequest request) {
        recipient.setRecipientType(normalizeRecipientType(request.recipientType(), true));
        recipient.setStudentId(request.studentId());
        recipient.setStudentNo(QueryFilterSupport.trimToNull(request.studentNo()));
        recipient.setRecipientName(QueryFilterSupport.trimToNull(request.recipientName()));
        recipient.setMajor(QueryFilterSupport.trimToNull(request.major()));
        recipient.setGrade(QueryFilterSupport.trimToNull(request.grade()));
        recipient.setClassName(QueryFilterSupport.trimToNull(request.className()));
        recipient.setAwardIntro(QueryFilterSupport.trimToNull(request.awardIntro()));
        recipient.setAdvancedDeeds(QueryFilterSupport.trimToNull(request.advancedDeeds()));
        recipient.setPhotoFileId(request.photoFileId());
        recipient.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        recipient.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
        recipient.setDisplayStartAt(request.displayStartAt());
        recipient.setDisplayEndAt(request.displayEndAt());
        recipient.setImportTaskId(request.importTaskId());
    }

    private void populateMember(HonorRecipientMember member, HonorRecipientMemberUpsertRequest request) {
        member.setStudentId(request.studentId());
        member.setStudentNo(QueryFilterSupport.trimToNull(request.studentNo()));
        member.setStudentName(QueryFilterSupport.trimToNull(request.studentName()));
        member.setMajor(QueryFilterSupport.trimToNull(request.major()));
        member.setGrade(QueryFilterSupport.trimToNull(request.grade()));
        member.setClassName(QueryFilterSupport.trimToNull(request.className()));
        member.setMemberRole(QueryFilterSupport.trimToNull(request.memberRole()));
        member.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
    }

    private void populateAttachment(HonorRecipientAttachment attachment, HonorRecipientAttachmentUpsertRequest request) {
        String attachmentType = normalizeAttachmentType(request.attachmentType());
        PlatformFileUploadRecord upload = request.fileId() == null ? null : uploadRecordRepository.findById(request.fileId())
                .orElseThrow(() -> new BusinessException("上传文件不存在"));
        String fileName = QueryFilterSupport.trimToNull(request.fileName());
        String contentType = QueryFilterSupport.trimToNull(request.contentType());
        Long fileSize = request.fileSize();
        String storagePath = QueryFilterSupport.trimToNull(request.storagePath());
        if (upload != null) {
            fileName = fileName == null ? upload.getFileName() : fileName;
            contentType = contentType == null ? upload.getContentType() : contentType;
            fileSize = fileSize == null ? upload.getFileSize() : fileSize;
            storagePath = storagePath == null ? upload.getStoragePath() : storagePath;
        }
        if (fileName == null) {
            throw new BusinessException("文件名不能为空");
        }
        attachment.setFileId(request.fileId());
        attachment.setAttachmentType(attachmentType);
        attachment.setFileName(fileName);
        attachment.setContentType(contentType);
        attachment.setFileSize(fileSize);
        attachment.setStoragePath(storagePath);
        attachment.setCaption(QueryFilterSupport.trimToNull(request.caption()));
        attachment.setPublicVisible(request.publicVisible() == null || Boolean.TRUE.equals(request.publicVisible()));
        attachment.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
    }

    private void validateRecipientRequest(HonorRecipientUpsertRequest request) {
        validateRecipientType(request.recipientType());
        validateDisplayTime(request.displayStartAt(), request.displayEndAt());
        validateImportTask(request.importTaskId());
        if (request.photoFileId() != null && !uploadRecordRepository.existsById(request.photoFileId())) {
            throw new BusinessException("获得者照片文件不存在");
        }
    }

    private void validateRecipientType(String value) {
        normalizeRecipientType(value, true);
    }

    private void requireMatchingRecipientType(String showcaseRecipientType, String recipientType) {
        String normalized = normalizeRecipientType(recipientType, true);
        if (!normalized.equals(showcaseRecipientType)) {
            throw new BusinessException("获得者类型必须与所属荣誉展示模块一致");
        }
    }

    private String normalizeRecipientType(String value, boolean required) {
        String normalized = QueryFilterSupport.trimToNull(value);
        if (normalized == null) {
            if (required) {
                throw new BusinessException("获得者类型不能为空");
            }
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!RECIPIENT_TYPES.contains(normalized)) {
            throw new BusinessException("获得者类型仅支持 PERSONAL 或 COLLECTIVE");
        }
        return normalized;
    }

    private String normalizeAttachmentType(String value) {
        String normalized = QueryFilterSupport.trimToNull(value);
        if (normalized == null) {
            throw new BusinessException("附件类型不能为空");
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!ATTACHMENT_TYPES.contains(normalized)) {
            throw new BusinessException("附件类型仅支持 PHOTO、DOCUMENT、VIDEO、OTHER");
        }
        return normalized;
    }

    private void validateDisplayTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new BusinessException("展示结束时间不能早于开始时间");
        }
    }

    private void validateImportTask(Long importTaskId) {
        if (importTaskId != null && !dataImportTaskRepository.existsById(importTaskId)) {
            throw new BusinessException("导入任务不存在");
        }
    }

    private HonorShowcase requireShowcase(Long id) {
        return showcaseRepository.findById(id).orElseThrow(() -> new BusinessException("荣誉展示模块不存在"));
    }

    private HonorShowcase requireVisibleShowcase(Long id) {
        HonorShowcase showcase = requireShowcase(id);
        if (!isVisibleNow(showcase)) {
            throw new BusinessException("荣誉展示模块未公开或不在展示时效内");
        }
        return showcase;
    }

    private HonorRecipient requireRecipient(Long id) {
        return recipientRepository.findById(id).orElseThrow(() -> new BusinessException("荣誉获得者不存在"));
    }

    private boolean isVisibleNow(HonorShowcase showcase) {
        return Boolean.TRUE.equals(showcase.getPublicVisible())
                && isInDisplayWindow(showcase.getDisplayStartAt(), showcase.getDisplayEndAt());
    }

    private boolean isVisibleNow(HonorRecipient recipient) {
        return Boolean.TRUE.equals(recipient.getPublicVisible())
                && isInDisplayWindow(recipient.getDisplayStartAt(), recipient.getDisplayEndAt());
    }

    private boolean isInDisplayWindow(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();
        return (startAt == null || !now.isBefore(startAt)) && (endAt == null || !now.isAfter(endAt));
    }

    private <T> PageResponse<T> toPage(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, items.size());
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        int totalPages = (int) Math.ceil(items.size() / (double) normalizedSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), totalPages, normalizedPage, normalizedSize);
    }
}
