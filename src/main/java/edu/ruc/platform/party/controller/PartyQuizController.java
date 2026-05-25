package edu.ruc.platform.party.controller;

import edu.ruc.platform.auth.service.CurrentUserService;
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.common.enums.RoleType;
import edu.ruc.platform.common.security.RequireRoles;
import edu.ruc.platform.party.dto.*;
import edu.ruc.platform.party.service.PartyQuizApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/party/quizzes")
@RequiredArgsConstructor
public class PartyQuizController {

    private final PartyQuizApplicationService quizService;
    private final CurrentUserService currentUserService;

    @GetMapping("/banks")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<List<PartyQuestionBankResponse>> listBanks() {
        return ApiResponse.success(quizService.listQuestionBanks());
    }

    @GetMapping("/banks/{id}")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN, RoleType.COUNSELOR, RoleType.STUDENT})
    public ApiResponse<PartyQuestionBankResponse> getBankById(@Positive(message = "题库ID必须大于0") @PathVariable Long id) {
        return ApiResponse.success(quizService.getQuestionBankById(id));
    }

    @PostMapping("/banks")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<PartyQuestionBankResponse> createBank(@Valid @RequestBody PartyQuestionBankCreateRequest request) {
        return ApiResponse.success("题库创建成功", quizService.createQuestionBank(request));
    }

    @PostMapping("/banks/{bankId}/questions")
    @RequireRoles({RoleType.SUPER_ADMIN, RoleType.COLLEGE_ADMIN})
    public ApiResponse<PartyQuestionResponse> addQuestion(@Positive(message = "题库ID必须大于0") @PathVariable Long bankId,
                                                         @Valid @RequestBody PartyQuestionCreateRequest request) {
        return ApiResponse.success("题目添加成功", quizService.addQuestion(bankId, request));
    }

    @PostMapping("/submit")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER})
    public ApiResponse<PartyQuizResultResponse> submitQuiz(@Valid @RequestBody PartyQuizSubmitRequest request) {
        Long studentId = currentUserService.requireCurrentUser().userId();
        return ApiResponse.success("自测提交成功", quizService.submitQuiz(studentId, request));
    }

    @GetMapping("/records/student/{studentId}")
    @RequireRoles({RoleType.STUDENT, RoleType.LEAGUE_SECRETARY, RoleType.CLASS_LEADER,
            RoleType.COUNSELOR, RoleType.COLLEGE_ADMIN, RoleType.SUPER_ADMIN})
    public ApiResponse<List<PartyQuizRecordResponse>> listRecords(@Positive(message = "学生ID必须大于0") @PathVariable Long studentId) {
        return ApiResponse.success(quizService.listQuizRecords(studentId));
    }
}
