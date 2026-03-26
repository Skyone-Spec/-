package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import edu.ruc.platform.party.repository.PartyProgressRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyProgressService implements PartyProgressApplicationService {

    private final PartyProgressRecordRepository partyProgressRecordRepository;

    @Override
    public PartyProgressResponse getProgress(Long studentId) {
        var record = partyProgressRecordRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException("未找到党团流程记录"));
        return new PartyProgressResponse(
                record.getCurrentStage(),
                record.getStageStartDate(),
                (int) ChronoUnit.DAYS.between(record.getStageStartDate(), LocalDate.now()),
                resolveNextDeadline(record.getCurrentStage(), record.getStageStartDate()),
                record.getCompletedActions(),
                record.getNextAction(),
                resolveNextActionRule(record.getCurrentStage())
        );
    }

    @Override
    public PartyStageTimelineResponse getTimeline(Long studentId) {
        PartyProgressResponse progress = getProgress(studentId);
        return new PartyStageTimelineResponse(
                studentId,
                progress.currentStage(),
                List.of(
                        new PartyStageTimelineResponse.StageNode("入党申请人", true, false, progress.stageStartDate().minusMonths(3), progress.stageStartDate().minusMonths(2), 30, "提交申请书后完成基础登记", "提交申请书并完成基础登记"),
                        new PartyStageTimelineResponse.StageNode("党课学习小组", true, false, progress.stageStartDate().minusMonths(1), progress.stageStartDate(), 30, "完成党课学习与考核", "完成党课学习小组课程"),
                        new PartyStageTimelineResponse.StageNode(progress.currentStage(), true, true, progress.stageStartDate(), progress.nextDeadline(), resolveExpectedDurationDays(progress.currentStage()), progress.nextActionRule(), progress.completedActions()),
                        new PartyStageTimelineResponse.StageNode("发展对象", false, false, null, progress.stageStartDate().plusMonths(6), 90, "按学期节点完成推优与答辩", "满足培养期后按学期节点推进"),
                        new PartyStageTimelineResponse.StageNode("预备党员", false, false, null, null, 180, "支部审批通过后进入预备期", "等待支部审批"),
                        new PartyStageTimelineResponse.StageNode("正式党员", false, false, null, null, 365, "预备期满后转正", "转正完成")
                )
        );
    }

    @Override
    public List<ReminderResponse> listReminders(Long studentId) {
        PartyProgressResponse progress = getProgress(studentId);
        LocalDate today = LocalDate.now();
        return List.of(
                new ReminderResponse(
                        "后续动作提醒",
                        progress.nextAction(),
                        progress.nextDeadline(),
                        "MEDIUM",
                        progress.currentStage(),
                        progress.nextActionRule(),
                        (int) ChronoUnit.DAYS.between(today, progress.nextDeadline()),
                        progress.nextDeadline().isBefore(today)),
                new ReminderResponse(
                        "流程说明提醒",
                        "具体时间以学院每学期通知为准，系统仅提供固定流程辅助。",
                        progress.nextDeadline().plusMonths(2),
                        "LOW",
                        progress.currentStage(),
                        "按学院学期安排持续跟进",
                        (int) ChronoUnit.DAYS.between(today, progress.nextDeadline().plusMonths(2)),
                        progress.nextDeadline().plusMonths(2).isBefore(today))
        );
    }

    private LocalDate resolveNextDeadline(String currentStage, LocalDate stageStartDate) {
        return switch (currentStage) {
            case "积极分子" -> stageStartDate.plusMonths(3);
            case "发展对象" -> stageStartDate.plusMonths(6);
            case "预备党员" -> stageStartDate.plusYears(1);
            default -> stageStartDate.plusMonths(1);
        };
    }

    private String resolveNextActionRule(String currentStage) {
        return switch (currentStage) {
            case "积极分子" -> "培养期满 3 个月提交思想汇报";
            case "发展对象" -> "按学期节点完成推优与答辩";
            case "预备党员" -> "预备期满前完成转正材料";
            default -> "按学院通知推进下一阶段";
        };
    }

    private Integer resolveExpectedDurationDays(String currentStage) {
        return switch (currentStage) {
            case "积极分子" -> 90;
            case "发展对象" -> 90;
            case "预备党员" -> 365;
            default -> 30;
        };
    }
}
