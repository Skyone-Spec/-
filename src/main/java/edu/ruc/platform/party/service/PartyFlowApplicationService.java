package edu.ruc.platform.party.service;

import edu.ruc.platform.party.dto.*;
import java.util.List;

public interface PartyFlowApplicationService {
    List<PartyFlowTemplateResponse> listAllTemplates();
    PartyFlowTemplateResponse getTemplateById(Long id);
    PartyFlowTemplateResponse createTemplate(PartyFlowTemplateCreateRequest request);
    PartyFlowStageResponse addStage(Long flowId, PartyFlowStageCreateRequest request);
    void deleteTemplate(Long id);
}
