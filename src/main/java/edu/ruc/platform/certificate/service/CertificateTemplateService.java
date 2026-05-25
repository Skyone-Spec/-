package edu.ruc.platform.certificate.service;

import edu.ruc.platform.certificate.domain.CertificateTemplate;
import edu.ruc.platform.certificate.dto.*;
import edu.ruc.platform.certificate.repository.CertificateTemplateRepository;
import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class CertificateTemplateService implements CertificateTemplateApplicationService {

    private final CertificateTemplateRepository templateRepository;
    private final StudentProfileRepository studentProfileRepository;

    @Override
    public List<CertificateTemplateResponse> listAll() {
        return templateRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<CertificateTemplateResponse> listActive() {
        return templateRepository.findByIsActiveTrue().stream().map(this::toResponse).toList();
    }

    @Override
    public List<CertificateTemplateResponse> listByType(String certificateType) {
        return templateRepository.findByCertificateType(certificateType).stream().map(this::toResponse).toList();
    }

    @Override
    public CertificateTemplateResponse getById(Long id) {
        CertificateTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        return toResponse(template);
    }

    @Override
    public CertificateTemplateResponse getByCode(String templateCode) {
        CertificateTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new BusinessException("模板不存在: " + templateCode));
        return toResponse(template);
    }

    @Override
    public CertificateTemplateResponse create(CertificateTemplateCreateRequest request) {
        if (templateRepository.existsByTemplateCode(request.templateCode())) {
            throw new BusinessException("模板编码已存在: " + request.templateCode());
        }
        CertificateTemplate template = new CertificateTemplate();
        template.setTemplateCode(request.templateCode());
        template.setTemplateName(request.templateName());
        template.setCertificateType(request.certificateType());
        template.setTemplateContent(request.templateContent());
        template.setTemplateFilePath(request.templateFilePath());
        template.setOutputFormat(request.outputFormat() != null ? request.outputFormat() : "PDF");
        template.setDescription(request.description());
        template.setIsActive(true);
        return toResponse(templateRepository.save(template));
    }

    @Override
    public CertificateTemplateResponse update(Long id, CertificateTemplateUpdateRequest request) {
        CertificateTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        if (request.templateName() != null) template.setTemplateName(request.templateName());
        if (request.certificateType() != null) template.setCertificateType(request.certificateType());
        if (request.templateContent() != null) template.setTemplateContent(request.templateContent());
        if (request.templateFilePath() != null) template.setTemplateFilePath(request.templateFilePath());
        if (request.outputFormat() != null) template.setOutputFormat(request.outputFormat());
        if (request.isActive() != null) template.setIsActive(request.isActive());
        if (request.description() != null) template.setDescription(request.description());
        return toResponse(templateRepository.save(template));
    }

    @Override
    public void delete(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new BusinessException("模板不存在");
        }
        templateRepository.deleteById(id);
    }

    @Override
    public String renderPreview(Long templateId, Long studentId) {
        CertificateTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("模板不存在"));
        StudentProfile student = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));

        String content = template.getTemplateContent();
        if (content == null || content.isBlank()) {
            content = generateDefaultContent(template.getCertificateType());
        }

        content = content.replace("{{studentName}}", student.getName() != null ? student.getName() : "");
        content = content.replace("{{studentNo}}", student.getStudentNo() != null ? student.getStudentNo() : "");
        content = content.replace("{{major}}", student.getMajor() != null ? student.getMajor() : "");
        content = content.replace("{{grade}}", student.getGrade() != null ? student.getGrade() : "");
        content = content.replace("{{className}}", student.getClassName() != null ? student.getClassName() : "");
        content = content.replace("{{certificateType}}", template.getCertificateType());
        content = content.replace("{{generatedDate}}", LocalDateTime.now().toString().substring(0, 10));
        return content;
    }

    private String generateDefaultContent(String certificateType) {
        return switch (certificateType) {
            case "在读证明" -> """
                    在读证明
                    
                    兹证明 {{studentName}}（学号：{{studentNo}}）系我院 {{major}} 专业 {{grade}} {{className}} 班学生，现为在籍在读状态。
                    
                    特此证明。
                    
                    学院（盖章）
                    {{generatedDate}}
                    """;
            case "党员身份证明" -> """
                    党员身份证明
                    
                    兹证明 {{studentName}}（学号：{{studentNo}}）系我院 {{major}} 专业学生，中共党员。
                    
                    特此证明。
                    
                    学院党委（盖章）
                    {{generatedDate}}
                    """;
            default -> """
                    证明
                    
                    兹证明 {{studentName}}（学号：{{studentNo}}）系我院 {{major}} 专业 {{grade}} {{className}} 班学生。
                    
                    特此证明。
                    
                    学院（盖章）
                    {{generatedDate}}
                    """;
        };
    }

    private CertificateTemplateResponse toResponse(CertificateTemplate template) {
        return new CertificateTemplateResponse(
                template.getId(), template.getTemplateCode(), template.getTemplateName(),
                template.getCertificateType(), template.getTemplateContent(),
                template.getTemplateFilePath(), template.getOutputFormat(),
                template.getIsActive(), template.getDescription(),
                template.getUpdatedBy(), template.getCreatedAt(), template.getUpdatedAt()
        );
    }
}
