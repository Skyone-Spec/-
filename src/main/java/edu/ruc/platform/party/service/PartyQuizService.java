package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.domain.*;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyQuizService implements PartyQuizApplicationService {

    private final PartyQuestionBankRepository bankRepository;
    private final PartyQuestionRepository questionRepository;
    private final PartyQuizRecordRepository recordRepository;

    @Override
    public List<PartyQuestionBankResponse> listQuestionBanks() {
        return bankRepository.findByIsActiveTrue().stream()
                .map(bank -> toResponse(bank, false))
                .toList();
    }

    @Override
    public PartyQuestionBankResponse getQuestionBankById(Long id) {
        PartyQuestionBank bank = bankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("题库不存在"));
        return toResponse(bank, true);
    }

    @Override
    public PartyQuestionBankResponse createQuestionBank(PartyQuestionBankCreateRequest request) {
        if (bankRepository.findByBankCode(request.bankCode()).isPresent()) {
            throw new BusinessException("题库编码已存在: " + request.bankCode());
        }
        PartyQuestionBank bank = new PartyQuestionBank();
        bank.setBankCode(request.bankCode());
        bank.setBankName(request.bankName());
        bank.setCategory(request.category());
        bank.setDescription(request.description());
        bank.setQuestionCount(0);
        bank.setIsActive(true);
        return toResponse(bankRepository.save(bank), false);
    }

    @Override
    public PartyQuestionResponse addQuestion(Long bankId, PartyQuestionCreateRequest request) {
        PartyQuestionBank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new BusinessException("题库不存在"));
        PartyQuestion question = new PartyQuestion();
        question.setBankId(bankId);
        question.setSeqNo(request.seqNo());
        question.setQuestionText(request.questionText());
        question.setOptions(request.options());
        question.setCorrectAnswer(request.correctAnswer());
        question.setExplanation(request.explanation());
        question.setScore(request.score() != null ? request.score() : 1);
        question = questionRepository.save(question);

        bank.setQuestionCount((int) questionRepository.countByBankId(bankId));
        bankRepository.save(bank);

        return toQuestionResponse(question);
    }

    @Override
    public PartyQuizResultResponse submitQuiz(Long studentId, PartyQuizSubmitRequest request) {
        PartyQuestionBank bank = bankRepository.findById(request.bankId())
                .orElseThrow(() -> new BusinessException("题库不存在"));

        List<PartyQuestion> questions = questionRepository.findByBankIdOrderBySeqNo(request.bankId());
        Map<Long, String> answers = request.answers();

        int correctCount = 0;
        int totalScore = 0;
        int earnedScore = 0;
        List<PartyQuizResultResponse.PartyQuizAnswerDetail> details = new ArrayList<>();

        for (PartyQuestion question : questions) {
            String userAnswer = answers.get(question.getId());
            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(userAnswer != null ? userAnswer.trim() : "");
            if (isCorrect) {
                correctCount++;
                earnedScore += question.getScore();
            }
            totalScore += question.getScore();

            details.add(new PartyQuizResultResponse.PartyQuizAnswerDetail(
                    question.getId(), question.getQuestionText(), userAnswer,
                    question.getCorrectAnswer(), isCorrect, isCorrect ? question.getScore() : 0,
                    question.getExplanation()
            ));
        }

        boolean passed = earnedScore >= totalScore * 0.6;

        PartyQuizRecord record = new PartyQuizRecord();
        record.setStudentId(studentId);
        record.setBankId(request.bankId());
        record.setTotalQuestions(questions.size());
        record.setCorrectCount(correctCount);
        record.setScore(earnedScore);
        record.setTotalScore(totalScore);
        record.setPassed(passed);
        record.setCompletedAt(LocalDateTime.now());
        record = recordRepository.save(record);

        return new PartyQuizResultResponse(
                record.getId(), studentId, request.bankId(), bank.getBankName(),
                questions.size(), correctCount, earnedScore, totalScore, passed,
                details, record.getCompletedAt()
        );
    }

    @Override
    public List<PartyQuizRecordResponse> listQuizRecords(Long studentId) {
        return recordRepository.findByStudentIdOrderByCompletedAtDesc(studentId).stream()
                .map(record -> {
                    String bankName = bankRepository.findById(record.getBankId())
                            .map(PartyQuestionBank::getBankName)
                            .orElse("未知题库");
                    return new PartyQuizRecordResponse(
                            record.getId(), record.getBankId(), bankName,
                            record.getTotalQuestions(), record.getCorrectCount(),
                            record.getScore(), record.getTotalScore(),
                            record.getPassed(), record.getCompletedAt()
                    );
                })
                .toList();
    }

    private PartyQuestionBankResponse toResponse(PartyQuestionBank bank, boolean includeQuestions) {
        List<PartyQuestionResponse> questions = includeQuestions ?
                questionRepository.findByBankIdOrderBySeqNo(bank.getId())
                        .stream().map(this::toQuestionResponse).toList() :
                List.of();
        return new PartyQuestionBankResponse(
                bank.getId(), bank.getBankCode(), bank.getBankName(), bank.getCategory(),
                bank.getQuestionCount(), bank.getIsActive(), bank.getDescription(), questions
        );
    }

    private PartyQuestionResponse toQuestionResponse(PartyQuestion question) {
        return new PartyQuestionResponse(
                question.getId(), question.getSeqNo(), question.getQuestionText(),
                question.getOptions(), question.getCorrectAnswer(),
                question.getExplanation(), question.getScore()
        );
    }
}
