package edu.ruc.platform.certificate.service;

import edu.ruc.platform.admin.repository.AdminOperationLogRepository;
import edu.ruc.platform.auth.dto.AuthenticatedUser;
import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.auth.service.StudentDataScopeService;
import edu.ruc.platform.certificate.domain.CertificateRequest;
import edu.ruc.platform.certificate.dto.ApprovalActionRequest;
import edu.ruc.platform.certificate.repository.ApprovalActionLogRepository;
import edu.ruc.platform.certificate.repository.CertificateRequestRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.repository.AdvisorScopeBindingRepository;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CertificateServiceUnitTest {

    @Test
    void counselorCannotFinalApproveCounselorApprovedRequestInNonMockMode() {
        CertificateRequestRepository certificateRequestRepository = mock(CertificateRequestRepository.class);
        ApprovalActionLogRepository approvalActionLogRepository = mock(ApprovalActionLogRepository.class);
        AdminOperationLogRepository adminOperationLogRepository = mock(AdminOperationLogRepository.class);
        StudentProfileRepository studentProfileRepository = mock(StudentProfileRepository.class);
        AdvisorScopeBindingRepository advisorScopeBindingRepository = mock(AdvisorScopeBindingRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        StudentDataScopeService studentDataScopeService = mock(StudentDataScopeService.class);

        CertificateService service = new CertificateService(
                certificateRequestRepository,
                approvalActionLogRepository,
                adminOperationLogRepository,
                studentProfileRepository,
                advisorScopeBindingRepository,
                currentUserService,
                studentDataScopeService
        );

        CertificateRequest request = new CertificateRequest();
        request.setId(1002L);
        request.setStudentId(10001L);
        request.setCertificateType("党员身份证明");
        request.setStatus("COUNSELOR_APPROVED");
        request.setReason("组织关系转接");
        request.setApprovalLevel(2);
        request.setCurrentApproverRole("COLLEGE_ADMIN");
        request.setWithdrawalDeadline(LocalDateTime.now().plusDays(1));

        when(certificateRequestRepository.findById(1002L)).thenReturn(Optional.of(request));
        when(currentUserService.requireCurrentUser()).thenReturn(new AuthenticatedUser(
                20001L,
                null,
                "teacher01",
                "COUNSELOR",
                null,
                "胡浩老师",
                null,
                "2023级"
        ));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.handleApproval(1002L, new ApprovalActionRequest("approve", "越权终审")));

        assertEquals("辅导员初审通过后的申请仅学院管理员可终审", exception.getMessage());
        verify(certificateRequestRepository, never()).save(any(CertificateRequest.class));
        verify(approvalActionLogRepository, never()).save(any());
        verify(adminOperationLogRepository, never()).save(any());
    }
}
