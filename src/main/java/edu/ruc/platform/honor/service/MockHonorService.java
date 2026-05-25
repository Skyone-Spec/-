package edu.ruc.platform.honor.service;

import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.common.support.QueryFilterSupport;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockHonorService implements HonorApplicationService {

    private static final Set<String> RECIPIENT_TYPES = Set.of("PERSONAL", "COLLECTIVE");
    private static final Set<String> ATTACHMENT_TYPES = Set.of("PHOTO", "DOCUMENT", "VIDEO", "OTHER");

    private final CurrentUserService currentUserService;
    private final AtomicLong ids = new AtomicLong(1000);
    private final List<ShowcaseRow> showcases = new ArrayList<>();
    private final List<RecipientRow> recipients = new ArrayList<>();
    private final List<MemberRow> members = new ArrayList<>();
    private final List<AttachmentRow> attachments = new ArrayList<>();

    @Override
    public PageResponse<HonorShowcaseAdminResponse> pageAdminShowcases(HonorShowcaseFilterRequest request, int page, int size) {
        return toPage(filterShowcases(request).stream().map(item -> toAdminShowcase(item, false)).toList(), page, size);
    }

    @Override
    public HonorShowcaseAdminResponse getAdminShowcase(Long id) {
        return toAdminShowcase(requireShowcase(id), true);
    }

    @Override
    public HonorShowcaseAdminResponse createShowcase(HonorShowcaseUpsertRequest request) {
        validateShowcase(request);
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        ShowcaseRow row = new ShowcaseRow(ids.incrementAndGet(), request.awardYear(), trim(request.honorCategory()),
                normalizeRecipientType(request.recipientType(), true), trim(request.title()), trim(request.description()),
                Boolean.TRUE.equals(request.publicVisible()), request.displayOrder() == null ? 0 : request.displayOrder(),
                request.displayStartAt(), request.displayEndAt(), request.importTaskId(), user.name(), user.name(),
                LocalDateTime.now(), LocalDateTime.now());
        showcases.add(row);
        return toAdminShowcase(row, true);
    }

    @Override
    public HonorShowcaseAdminResponse updateShowcase(Long id, HonorShowcaseUpsertRequest request) {
        validateShowcase(request);
        ShowcaseRow row = requireShowcase(id);
        row.awardYear = request.awardYear();
        row.honorCategory = trim(request.honorCategory());
        row.recipientType = normalizeRecipientType(request.recipientType(), true);
        row.title = trim(request.title());
        row.description = trim(request.description());
        row.publicVisible = Boolean.TRUE.equals(request.publicVisible());
        row.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        row.displayStartAt = request.displayStartAt();
        row.displayEndAt = request.displayEndAt();
        row.importTaskId = request.importTaskId();
        row.updatedBy = currentUserService.requireCurrentUser().name();
        row.updatedAt = LocalDateTime.now();
        return toAdminShowcase(row, true);
    }

    @Override
    public void deleteShowcase(Long id) {
        requireShowcase(id);
        List<Long> recipientIds = recipients.stream().filter(item -> item.showcaseId.equals(id)).map(item -> item.id).toList();
        attachments.removeIf(item -> recipientIds.contains(item.recipientId));
        members.removeIf(item -> recipientIds.contains(item.recipientId));
        recipients.removeIf(item -> item.showcaseId.equals(id));
        showcases.removeIf(item -> item.id.equals(id));
    }

    @Override
    public PageResponse<HonorRecipientAdminResponse> pageAdminRecipients(Long showcaseId, HonorRecipientFilterRequest request, int page, int size) {
        requireShowcase(showcaseId);
        return toPage(filterRecipients(showcaseId, request).stream().map(item -> toAdminRecipient(item, false)).toList(), page, size);
    }

    @Override
    public HonorRecipientAdminResponse getAdminRecipient(Long recipientId) {
        return toAdminRecipient(requireRecipient(recipientId), true);
    }

    @Override
    public HonorRecipientAdminResponse createRecipient(Long showcaseId, HonorRecipientUpsertRequest request) {
        ShowcaseRow showcase = requireShowcase(showcaseId);
        validateRecipient(request);
        requireMatchingRecipientType(showcase.recipientType, request.recipientType());
        AuthenticatedUser user = currentUserService.requireCurrentUser();
        RecipientRow row = new RecipientRow(ids.incrementAndGet(), showcaseId, normalizeRecipientType(request.recipientType(), true),
                request.studentId(), trim(request.studentNo()), trim(request.recipientName()), trim(request.major()), trim(request.grade()),
                trim(request.className()), trim(request.awardIntro()), trim(request.advancedDeeds()), request.photoFileId(),
                Boolean.TRUE.equals(request.publicVisible()), request.displayOrder() == null ? 0 : request.displayOrder(),
                request.displayStartAt(), request.displayEndAt(), request.importTaskId(), user.name(), user.name(),
                LocalDateTime.now(), LocalDateTime.now());
        recipients.add(row);
        return toAdminRecipient(row, true);
    }

    @Override
    public HonorRecipientAdminResponse updateRecipient(Long recipientId, HonorRecipientUpsertRequest request) {
        validateRecipient(request);
        RecipientRow row = requireRecipient(recipientId);
        requireMatchingRecipientType(requireShowcase(row.showcaseId).recipientType, request.recipientType());
        row.recipientType = normalizeRecipientType(request.recipientType(), true);
        row.studentId = request.studentId();
        row.studentNo = trim(request.studentNo());
        row.recipientName = trim(request.recipientName());
        row.major = trim(request.major());
        row.grade = trim(request.grade());
        row.className = trim(request.className());
        row.awardIntro = trim(request.awardIntro());
        row.advancedDeeds = trim(request.advancedDeeds());
        row.photoFileId = request.photoFileId();
        row.publicVisible = Boolean.TRUE.equals(request.publicVisible());
        row.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        row.displayStartAt = request.displayStartAt();
        row.displayEndAt = request.displayEndAt();
        row.importTaskId = request.importTaskId();
        row.updatedBy = currentUserService.requireCurrentUser().name();
        row.updatedAt = LocalDateTime.now();
        return toAdminRecipient(row, true);
    }

    @Override
    public void deleteRecipient(Long recipientId) {
        requireRecipient(recipientId);
        attachments.removeIf(item -> item.recipientId.equals(recipientId));
        members.removeIf(item -> item.recipientId.equals(recipientId));
        recipients.removeIf(item -> item.id.equals(recipientId));
    }

    @Override
    public HonorRecipientMemberResponse createMember(Long recipientId, HonorRecipientMemberUpsertRequest request) {
        RecipientRow recipient = requireRecipient(recipientId);
        if (!"COLLECTIVE".equals(recipient.recipientType)) {
            throw new BusinessException("仅集体荣誉获得者可维护成员");
        }
        MemberRow row = new MemberRow(ids.incrementAndGet(), recipientId, request.studentId(), trim(request.studentNo()),
                trim(request.studentName()), trim(request.major()), trim(request.grade()), trim(request.className()),
                trim(request.memberRole()), request.displayOrder() == null ? 0 : request.displayOrder(), LocalDateTime.now());
        members.add(row);
        return toMember(row);
    }

    @Override
    public HonorRecipientMemberResponse updateMember(Long memberId, HonorRecipientMemberUpsertRequest request) {
        MemberRow row = members.stream().filter(item -> item.id.equals(memberId)).findFirst()
                .orElseThrow(() -> new BusinessException("荣誉成员不存在"));
        row.studentId = request.studentId();
        row.studentNo = trim(request.studentNo());
        row.studentName = trim(request.studentName());
        row.major = trim(request.major());
        row.grade = trim(request.grade());
        row.className = trim(request.className());
        row.memberRole = trim(request.memberRole());
        row.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        return toMember(row);
    }

    @Override
    public void deleteMember(Long memberId) {
        members.removeIf(item -> item.id.equals(memberId));
    }

    @Override
    public HonorRecipientAttachmentResponse createAttachment(Long recipientId, HonorRecipientAttachmentUpsertRequest request) {
        requireRecipient(recipientId);
        AttachmentRow row = new AttachmentRow(ids.incrementAndGet(), recipientId, request.fileId(), normalizeAttachmentType(request.attachmentType()),
                requireFileName(request.fileName()), trim(request.contentType()), request.fileSize(), trim(request.storagePath()),
                trim(request.caption()), request.publicVisible() == null || Boolean.TRUE.equals(request.publicVisible()),
                request.displayOrder() == null ? 0 : request.displayOrder(), LocalDateTime.now());
        attachments.add(row);
        return toAttachment(row);
    }

    @Override
    public HonorRecipientAttachmentResponse updateAttachment(Long attachmentId, HonorRecipientAttachmentUpsertRequest request) {
        AttachmentRow row = attachments.stream().filter(item -> item.id.equals(attachmentId)).findFirst()
                .orElseThrow(() -> new BusinessException("荣誉附件不存在"));
        row.fileId = request.fileId();
        row.attachmentType = normalizeAttachmentType(request.attachmentType());
        row.fileName = requireFileName(request.fileName());
        row.contentType = trim(request.contentType());
        row.fileSize = request.fileSize();
        row.storagePath = trim(request.storagePath());
        row.caption = trim(request.caption());
        row.publicVisible = request.publicVisible() == null || Boolean.TRUE.equals(request.publicVisible());
        row.displayOrder = request.displayOrder() == null ? 0 : request.displayOrder();
        return toAttachment(row);
    }

    @Override
    public void deleteAttachment(Long attachmentId) {
        attachments.removeIf(item -> item.id.equals(attachmentId));
    }

    @Override
    public PageResponse<HonorShowcaseStudentResponse> pageStudentShowcases(HonorShowcaseFilterRequest request, int page, int size) {
        return toPage(filterShowcases(request).stream()
                .filter(this::visible)
                .map(item -> toStudentShowcase(item, false))
                .filter(item -> item.recipientCount() > 0)
                .toList(), page, size);
    }

    @Override
    public HonorShowcaseStudentResponse getStudentShowcase(Long id) {
        ShowcaseRow row = requireShowcase(id);
        if (!visible(row)) {
            throw new BusinessException("荣誉展示模块未公开或不在展示时效内");
        }
        return toStudentShowcase(row, true);
    }

    @Override
    public List<HonorRecipientStudentResponse> listStudentRecipients(Long showcaseId) {
        ShowcaseRow row = requireShowcase(showcaseId);
        if (!visible(row)) {
            throw new BusinessException("荣誉展示模块未公开或不在展示时效内");
        }
        return visibleRecipients(showcaseId).stream().map(this::toStudentRecipient).toList();
    }

    @Override
    public HonorRecipientStudentResponse getStudentRecipient(Long recipientId) {
        RecipientRow row = requireRecipient(recipientId);
        ShowcaseRow showcase = requireShowcase(row.showcaseId);
        if (!visible(showcase) || !visible(row)) {
            throw new BusinessException("荣誉获得者未公开或不在展示时效内");
        }
        return toStudentRecipient(row);
    }

    private List<ShowcaseRow> filterShowcases(HonorShowcaseFilterRequest request) {
        HonorShowcaseFilterRequest safe = request == null ? new HonorShowcaseFilterRequest(null, null, null, null, null) : request;
        String category = trim(safe.honorCategory());
        String recipientType = normalizeRecipientType(safe.recipientType(), false);
        String keyword = trim(safe.keyword());
        return showcases.stream()
                .filter(item -> safe.awardYear() == null || safe.awardYear().equals(item.awardYear))
                .filter(item -> category == null || QueryFilterSupport.containsIgnoreCase(item.honorCategory, category))
                .filter(item -> recipientType == null || recipientType.equals(item.recipientType))
                .filter(item -> safe.publicVisible() == null || safe.publicVisible().equals(item.publicVisible))
                .filter(item -> keyword == null || QueryFilterSupport.containsIgnoreCase(item.title, keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.description, keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.honorCategory, keyword))
                .sorted(Comparator.comparingInt((ShowcaseRow item) -> item.displayOrder)
                        .thenComparing(ShowcaseRow::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    private List<RecipientRow> filterRecipients(Long showcaseId, HonorRecipientFilterRequest request) {
        HonorRecipientFilterRequest safe = request == null ? new HonorRecipientFilterRequest(null, null, null) : request;
        String recipientType = normalizeRecipientType(safe.recipientType(), false);
        String keyword = trim(safe.keyword());
        return recipients.stream()
                .filter(item -> item.showcaseId.equals(showcaseId))
                .filter(item -> recipientType == null || recipientType.equals(item.recipientType))
                .filter(item -> safe.publicVisible() == null || safe.publicVisible().equals(item.publicVisible))
                .filter(item -> keyword == null || QueryFilterSupport.containsIgnoreCase(item.recipientName, keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.studentNo, keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.awardIntro, keyword)
                        || QueryFilterSupport.containsIgnoreCase(item.advancedDeeds, keyword))
                .sorted(Comparator.comparingInt((RecipientRow item) -> item.displayOrder)
                        .thenComparing(RecipientRow::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    private HonorShowcaseAdminResponse toAdminShowcase(ShowcaseRow row, boolean includeRecipients) {
        List<HonorRecipientAdminResponse> children = includeRecipients
                ? filterRecipients(row.id, null).stream().map(item -> toAdminRecipient(item, true)).toList()
                : List.of();
        return new HonorShowcaseAdminResponse(row.id, row.awardYear, row.honorCategory, row.recipientType, row.title, row.description,
                row.publicVisible, row.displayOrder, row.displayStartAt, row.displayEndAt, row.importTaskId, row.createdByName,
                row.updatedBy, row.createdAt, row.updatedAt, recipients.stream().filter(item -> item.showcaseId.equals(row.id)).count(), children);
    }

    private HonorRecipientAdminResponse toAdminRecipient(RecipientRow row, boolean includeChildren) {
        List<HonorRecipientMemberResponse> childMembers = includeChildren
                ? members.stream().filter(item -> item.recipientId.equals(row.id)).sorted(Comparator.comparingInt(MemberRow::displayOrder)).map(this::toMember).toList()
                : List.of();
        List<HonorRecipientAttachmentResponse> childAttachments = includeChildren
                ? attachments.stream().filter(item -> item.recipientId.equals(row.id)).sorted(Comparator.comparingInt(AttachmentRow::displayOrder)).map(this::toAttachment).toList()
                : List.of();
        return new HonorRecipientAdminResponse(row.id, row.showcaseId, row.recipientType, row.studentId, row.studentNo, row.recipientName,
                row.major, row.grade, row.className, row.awardIntro, row.advancedDeeds, row.photoFileId, row.publicVisible,
                row.displayOrder, row.displayStartAt, row.displayEndAt, row.importTaskId, row.createdByName, row.updatedBy,
                row.createdAt, row.updatedAt, childMembers, childAttachments);
    }

    private HonorShowcaseStudentResponse toStudentShowcase(ShowcaseRow row, boolean includeRecipients) {
        List<HonorRecipientStudentResponse> childRecipients = includeRecipients
                ? visibleRecipients(row.id).stream().map(this::toStudentRecipient).toList()
                : List.of();
        return new HonorShowcaseStudentResponse(row.id, row.awardYear, row.honorCategory, row.recipientType, row.title,
                row.description, visibleRecipients(row.id).size(), childRecipients);
    }

    private HonorRecipientStudentResponse toStudentRecipient(RecipientRow row) {
        return new HonorRecipientStudentResponse(row.id, row.showcaseId, row.recipientType, row.recipientName, row.major,
                row.grade, row.className, row.awardIntro, row.advancedDeeds, row.photoFileId,
                members.stream().filter(item -> item.recipientId.equals(row.id)).sorted(Comparator.comparingInt(MemberRow::displayOrder))
                        .map(item -> new HonorRecipientMemberStudentResponse(item.id, item.studentName, item.major, item.grade, item.className, item.memberRole))
                        .toList(),
                attachments.stream().filter(item -> item.recipientId.equals(row.id) && item.publicVisible).sorted(Comparator.comparingInt(AttachmentRow::displayOrder))
                        .map(item -> new HonorRecipientAttachmentStudentResponse(item.id, item.fileId, item.attachmentType, item.fileName,
                                item.contentType, item.fileSize, item.storagePath, item.caption))
                        .toList());
    }

    private HonorRecipientMemberResponse toMember(MemberRow row) {
        return new HonorRecipientMemberResponse(row.id, row.recipientId, row.studentId, row.studentNo, row.studentName, row.major,
                row.grade, row.className, row.memberRole, row.displayOrder);
    }

    private HonorRecipientAttachmentResponse toAttachment(AttachmentRow row) {
        return new HonorRecipientAttachmentResponse(row.id, row.recipientId, row.fileId, row.attachmentType, row.fileName, row.contentType,
                row.fileSize, row.storagePath, row.caption, row.publicVisible, row.displayOrder);
    }

    private List<RecipientRow> visibleRecipients(Long showcaseId) {
        return filterRecipients(showcaseId, null).stream().filter(this::visible).toList();
    }

    private ShowcaseRow requireShowcase(Long id) {
        return showcases.stream().filter(item -> item.id.equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException("荣誉展示模块不存在"));
    }

    private RecipientRow requireRecipient(Long id) {
        return recipients.stream().filter(item -> item.id.equals(id)).findFirst()
                .orElseThrow(() -> new BusinessException("荣誉获得者不存在"));
    }

    private void validateShowcase(HonorShowcaseUpsertRequest request) {
        normalizeRecipientType(request.recipientType(), true);
        validateTime(request.displayStartAt(), request.displayEndAt());
    }

    private void validateRecipient(HonorRecipientUpsertRequest request) {
        normalizeRecipientType(request.recipientType(), true);
        validateTime(request.displayStartAt(), request.displayEndAt());
    }

    private void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new BusinessException("展示结束时间不能早于开始时间");
        }
    }

    private boolean visible(ShowcaseRow row) {
        return row.publicVisible && inWindow(row.displayStartAt, row.displayEndAt);
    }

    private boolean visible(RecipientRow row) {
        return row.publicVisible && inWindow(row.displayStartAt, row.displayEndAt);
    }

    private boolean inWindow(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime now = LocalDateTime.now();
        return (startAt == null || !now.isBefore(startAt)) && (endAt == null || !now.isAfter(endAt));
    }

    private String normalizeRecipientType(String value, boolean required) {
        String normalized = trim(value);
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
        String normalized = trim(value);
        if (normalized == null) {
            throw new BusinessException("附件类型不能为空");
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (!ATTACHMENT_TYPES.contains(normalized)) {
            throw new BusinessException("附件类型仅支持 PHOTO、DOCUMENT、VIDEO、OTHER");
        }
        return normalized;
    }

    private void requireMatchingRecipientType(String showcaseRecipientType, String recipientType) {
        String normalized = normalizeRecipientType(recipientType, true);
        if (!normalized.equals(showcaseRecipientType)) {
            throw new BusinessException("获得者类型必须与所属荣誉展示模块一致");
        }
    }

    private String requireFileName(String value) {
        String fileName = trim(value);
        if (fileName == null) {
            throw new BusinessException("文件名不能为空");
        }
        return fileName;
    }

    private String trim(String value) {
        return QueryFilterSupport.trimToNull(value);
    }

    private <T> PageResponse<T> toPage(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int fromIndex = Math.min(normalizedPage * normalizedSize, items.size());
        int toIndex = Math.min(fromIndex + normalizedSize, items.size());
        int totalPages = (int) Math.ceil(items.size() / (double) normalizedSize);
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), totalPages, normalizedPage, normalizedSize);
    }

    private static final class ShowcaseRow {
        private final Long id;
        private Integer awardYear;
        private String honorCategory;
        private String recipientType;
        private String title;
        private String description;
        private Boolean publicVisible;
        private Integer displayOrder;
        private LocalDateTime displayStartAt;
        private LocalDateTime displayEndAt;
        private Long importTaskId;
        private String createdByName;
        private String updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private ShowcaseRow(Long id, Integer awardYear, String honorCategory, String recipientType, String title, String description,
                            Boolean publicVisible, Integer displayOrder, LocalDateTime displayStartAt, LocalDateTime displayEndAt,
                            Long importTaskId, String createdByName, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.awardYear = awardYear;
            this.honorCategory = honorCategory;
            this.recipientType = recipientType;
            this.title = title;
            this.description = description;
            this.publicVisible = publicVisible;
            this.displayOrder = displayOrder;
            this.displayStartAt = displayStartAt;
            this.displayEndAt = displayEndAt;
            this.importTaskId = importTaskId;
            this.createdByName = createdByName;
            this.updatedBy = updatedBy;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        private LocalDateTime createdAt() {
            return createdAt;
        }
    }

    private static final class RecipientRow {
        private final Long id;
        private final Long showcaseId;
        private String recipientType;
        private Long studentId;
        private String studentNo;
        private String recipientName;
        private String major;
        private String grade;
        private String className;
        private String awardIntro;
        private String advancedDeeds;
        private Long photoFileId;
        private Boolean publicVisible;
        private Integer displayOrder;
        private LocalDateTime displayStartAt;
        private LocalDateTime displayEndAt;
        private Long importTaskId;
        private String createdByName;
        private String updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private RecipientRow(Long id, Long showcaseId, String recipientType, Long studentId, String studentNo, String recipientName,
                             String major, String grade, String className, String awardIntro, String advancedDeeds, Long photoFileId,
                             Boolean publicVisible, Integer displayOrder, LocalDateTime displayStartAt, LocalDateTime displayEndAt,
                             Long importTaskId, String createdByName, String updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.showcaseId = showcaseId;
            this.recipientType = recipientType;
            this.studentId = studentId;
            this.studentNo = studentNo;
            this.recipientName = recipientName;
            this.major = major;
            this.grade = grade;
            this.className = className;
            this.awardIntro = awardIntro;
            this.advancedDeeds = advancedDeeds;
            this.photoFileId = photoFileId;
            this.publicVisible = publicVisible;
            this.displayOrder = displayOrder;
            this.displayStartAt = displayStartAt;
            this.displayEndAt = displayEndAt;
            this.importTaskId = importTaskId;
            this.createdByName = createdByName;
            this.updatedBy = updatedBy;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        private LocalDateTime createdAt() {
            return createdAt;
        }
    }

    private static final class MemberRow {
        private final Long id;
        private final Long recipientId;
        private Long studentId;
        private String studentNo;
        private String studentName;
        private String major;
        private String grade;
        private String className;
        private String memberRole;
        private Integer displayOrder;
        private final LocalDateTime createdAt;

        private MemberRow(Long id, Long recipientId, Long studentId, String studentNo, String studentName, String major, String grade,
                          String className, String memberRole, Integer displayOrder, LocalDateTime createdAt) {
            this.id = id;
            this.recipientId = recipientId;
            this.studentId = studentId;
            this.studentNo = studentNo;
            this.studentName = studentName;
            this.major = major;
            this.grade = grade;
            this.className = className;
            this.memberRole = memberRole;
            this.displayOrder = displayOrder;
            this.createdAt = createdAt;
        }

        private Integer displayOrder() {
            return displayOrder;
        }
    }

    private static final class AttachmentRow {
        private final Long id;
        private final Long recipientId;
        private Long fileId;
        private String attachmentType;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private String storagePath;
        private String caption;
        private Boolean publicVisible;
        private Integer displayOrder;
        private final LocalDateTime createdAt;

        private AttachmentRow(Long id, Long recipientId, Long fileId, String attachmentType, String fileName, String contentType,
                              Long fileSize, String storagePath, String caption, Boolean publicVisible, Integer displayOrder,
                              LocalDateTime createdAt) {
            this.id = id;
            this.recipientId = recipientId;
            this.fileId = fileId;
            this.attachmentType = attachmentType;
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.storagePath = storagePath;
            this.caption = caption;
            this.publicVisible = publicVisible;
            this.displayOrder = displayOrder;
            this.createdAt = createdAt;
        }

        private Integer displayOrder() {
            return displayOrder;
        }
    }
}
