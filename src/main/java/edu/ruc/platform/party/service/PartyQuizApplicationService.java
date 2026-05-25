package edu.ruc.platform.party.service;

import edu.ruc.platform.party.dto.*;
import java.util.List;

public interface PartyQuizApplicationService {
    List<PartyQuestionBankResponse> listQuestionBanks();
    PartyQuestionBankResponse getQuestionBankById(Long id);
    PartyQuestionBankResponse createQuestionBank(PartyQuestionBankCreateRequest request);
    PartyQuestionResponse addQuestion(Long bankId, PartyQuestionCreateRequest request);
    PartyQuizResultResponse submitQuiz(Long studentId, PartyQuizSubmitRequest request);
    List<PartyQuizRecordResponse> listQuizRecords(Long studentId);
}
