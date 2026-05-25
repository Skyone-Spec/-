package edu.ruc.platform.honor.service;

import edu.ruc.platform.common.api.PageResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAdminResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientAttachmentUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientFilterRequest;
import edu.ruc.platform.honor.dto.HonorRecipientMemberResponse;
import edu.ruc.platform.honor.dto.HonorRecipientMemberUpsertRequest;
import edu.ruc.platform.honor.dto.HonorRecipientStudentResponse;
import edu.ruc.platform.honor.dto.HonorRecipientUpsertRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseAdminResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseFilterRequest;
import edu.ruc.platform.honor.dto.HonorShowcaseStudentResponse;
import edu.ruc.platform.honor.dto.HonorShowcaseUpsertRequest;

import java.util.List;

public interface HonorApplicationService {

    PageResponse<HonorShowcaseAdminResponse> pageAdminShowcases(HonorShowcaseFilterRequest request, int page, int size);

    HonorShowcaseAdminResponse getAdminShowcase(Long id);

    HonorShowcaseAdminResponse createShowcase(HonorShowcaseUpsertRequest request);

    HonorShowcaseAdminResponse updateShowcase(Long id, HonorShowcaseUpsertRequest request);

    void deleteShowcase(Long id);

    PageResponse<HonorRecipientAdminResponse> pageAdminRecipients(Long showcaseId, HonorRecipientFilterRequest request, int page, int size);

    HonorRecipientAdminResponse getAdminRecipient(Long recipientId);

    HonorRecipientAdminResponse createRecipient(Long showcaseId, HonorRecipientUpsertRequest request);

    HonorRecipientAdminResponse updateRecipient(Long recipientId, HonorRecipientUpsertRequest request);

    void deleteRecipient(Long recipientId);

    HonorRecipientMemberResponse createMember(Long recipientId, HonorRecipientMemberUpsertRequest request);

    HonorRecipientMemberResponse updateMember(Long memberId, HonorRecipientMemberUpsertRequest request);

    void deleteMember(Long memberId);

    HonorRecipientAttachmentResponse createAttachment(Long recipientId, HonorRecipientAttachmentUpsertRequest request);

    HonorRecipientAttachmentResponse updateAttachment(Long attachmentId, HonorRecipientAttachmentUpsertRequest request);

    void deleteAttachment(Long attachmentId);

    PageResponse<HonorShowcaseStudentResponse> pageStudentShowcases(HonorShowcaseFilterRequest request, int page, int size);

    HonorShowcaseStudentResponse getStudentShowcase(Long id);

    List<HonorRecipientStudentResponse> listStudentRecipients(Long showcaseId);

    HonorRecipientStudentResponse getStudentRecipient(Long recipientId);
}
