package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.PartyFlowStage;
import edu.ruc.platform.party.domain.PartyFlowTemplate;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.repository.PartyFlowStageRepository;
import edu.ruc.platform.party.repository.PartyFlowTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyFlowService implements PartyFlowApplicationService {

    private final PartyFlowTemplateRepository templateRepository;
    private final PartyFlowStageRepository stageRepository;

    @Override
    public List<PartyFlowTemplateResponse> listAllTemplates() {
        return templateRepository.findAll().stream()
                .map(template -> toResponse(template, false))
                .toList();
    }

    @Override
    public PartyFlowTemplateResponse getTemplateById(Long id) {
        PartyFlowTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程模板不存在"));
        return toResponse(template, true);
    }

    @Override
    public PartyFlowTemplateResponse createTemplate(PartyFlowTemplateCreateRequest request) {
        if (templateRepository.findByFlowCode(request.flowCode()).isPresent()) {
            throw new BusinessException("流程编码已存在: " + request.flowCode());
        }
        PartyFlowTemplate template = new PartyFlowTemplate();
        template.setFlowCode(request.flowCode());
        template.setFlowName(request.flowName());
        template.setFlowType(request.flowType());
        template.setTotalStages(request.totalStages());
        template.setDescription(request.description());
        template.setIsActive(true);
        return toResponse(templateRepository.save(template), false);
    }

    @Override
    public PartyFlowStageResponse addStage(Long flowId, PartyFlowStageCreateRequest request) {
        if (!templateRepository.existsById(flowId)) {
            throw new BusinessException("流程模板不存在");
        }
        PartyFlowStage stage = new PartyFlowStage();
        stage.setFlowId(flowId);
        stage.setSeqNo(request.seqNo());
        stage.setStageCode(request.stageCode());
        stage.setStageName(request.stageName());
        stage.setDescription(request.description());
        stage.setRequiredMaterials(request.requiredMaterials());
        stage.setEstimatedDays(request.estimatedDays());
        stage.setReminderDaysBefore(request.reminderDaysBefore());
        stage.setIsActive(true);
        return toStageResponse(stageRepository.save(stage));
    }

    @Override
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new BusinessException("流程模板不存在");
        }
        templateRepository.deleteById(id);
    }

    private PartyFlowTemplateResponse toResponse(PartyFlowTemplate template, boolean includeStages) {
        List<PartyFlowStageResponse> stages = includeStages ?
                stageRepository.findByFlowIdAndIsActiveTrueOrderBySeqNo(template.getId())
                        .stream().map(this::toStageResponse).toList() :
                List.of();
        return new PartyFlowTemplateResponse(
                template.getId(), template.getFlowCode(), template.getFlowName(),
                template.getFlowType(), template.getTotalStages(), template.getDescription(),
                template.getIsActive(), stages, template.getCreatedAt()
        );
    }

    private PartyFlowStageResponse toStageResponse(PartyFlowStage stage) {
        return new PartyFlowStageResponse(
                stage.getId(), stage.getSeqNo(), stage.getStageCode(), stage.getStageName(),
                stage.getDescription(), stage.getRequiredMaterials(), stage.getEstimatedDays(),
                stage.getReminderDaysBefore(), stage.getIsActive()
        );
    }
}
