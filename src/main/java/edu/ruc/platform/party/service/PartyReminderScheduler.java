package edu.ruc.platform.party.service;

import edu.ruc.platform.party.domain.PartyFlowStage;
import edu.ruc.platform.party.domain.PartyFlowTemplate;
import edu.ruc.platform.party.domain.PartyMaterialSubmission;
import edu.ruc.platform.party.repository.PartyFlowStageRepository;
import edu.ruc.platform.party.repository.PartyFlowTemplateRepository;
import edu.ruc.platform.party.repository.PartyMaterialSubmissionRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyReminderScheduler {

    private final PartyMaterialSubmissionRepository submissionRepository;
    private final PartyFlowTemplateRepository flowTemplateRepository;
    private final PartyFlowStageRepository flowStageRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PartyReminderNotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndSendReminders() {
        log.info("开始检查党团节点提醒任务...");
        List<ReminderTask> tasks = collectReminderTasks();
        for (ReminderTask task : tasks) {
            try {
                notificationService.sendReminder(task);
                log.info("提醒已发送: studentId={}, type={}, stage={}", 
                        task.studentId(), task.type(), task.stageName());
            } catch (Exception e) {
                log.error("提醒发送失败: studentId={}, error={}", task.studentId(), e.getMessage());
            }
        }
        log.info("党团节点提醒检查完成，共处理 {} 条提醒", tasks.size());
    }

    private List<ReminderTask> collectReminderTasks() {
        List<ReminderTask> tasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        checkPendingMaterialReminders(tasks, now);
        checkFlowStageReminders(tasks, now);
        checkDeadlineReminders(tasks, now);

        return tasks;
    }

    private void checkPendingMaterialReminders(List<ReminderTask> tasks, LocalDateTime now) {
        List<PartyMaterialSubmission> pendingSubmissions = 
                submissionRepository.findByStatusIn(List.of("PENDING", "RETURNED"));
        
        for (PartyMaterialSubmission submission : pendingSubmissions) {
            long daysPending = ChronoUnit.DAYS.between(submission.getCreatedAt(), now);
            if (daysPending >= 3) {
                StudentProfile student = studentProfileRepository.findById(submission.getStudentId()).orElse(null);
                if (student != null) {
                    tasks.add(new ReminderTask(
                            submission.getStudentId(),
                            student.getName(),
                            "MATERIAL_PENDING",
                            submission.getStageName(),
                            "您提交的材料【" + submission.getTitle() + "】已等待" + daysPending + "天，请耐心等待审批或联系辅导员",
                            now
                    ));
                }
            }
        }
    }

    private void checkFlowStageReminders(List<ReminderTask> tasks, LocalDateTime now) {
        List<PartyFlowTemplate> templates = flowTemplateRepository.findByIsActiveTrue();
        
        for (PartyFlowTemplate template : templates) {
            List<PartyFlowStage> stages = flowStageRepository.findByFlowIdAndIsActiveTrueOrderBySeqNo(template.getId());
            
            for (PartyFlowStage stage : stages) {
                if (stage.getReminderDaysBefore() != null && stage.getReminderDaysBefore() > 0) {
                    checkStageDeadlineReminder(tasks, template, stage, now);
                }
            }
        }
    }

    private void checkStageDeadlineReminder(List<ReminderTask> tasks, PartyFlowTemplate template, 
                                            PartyFlowStage stage, LocalDateTime now) {
        List<PartyMaterialSubmission> stageSubmissions = 
                submissionRepository.findByFlowTypeAndStageName(template.getFlowType(), stage.getStageName());
        
        for (PartyMaterialSubmission submission : stageSubmissions) {
            if (!"APPROVED".equals(submission.getStatus())) {
                StudentProfile student = studentProfileRepository.findById(submission.getStudentId()).orElse(null);
                if (student != null) {
                    long daysSinceSubmit = ChronoUnit.DAYS.between(submission.getCreatedAt(), now);
                    if (daysSinceSubmit >= stage.getReminderDaysBefore()) {
                        tasks.add(new ReminderTask(
                                submission.getStudentId(),
                                student.getName(),
                                "STAGE_DEADLINE",
                                stage.getStageName(),
                                "【" + stage.getStageName() + "】阶段材料提交即将截止，请尽快完成",
                                now
                        ));
                    }
                }
            }
        }
    }

    private void checkDeadlineReminders(List<ReminderTask> tasks, LocalDateTime now) {
        List<PartyMaterialSubmission> submissionsNearDeadline = 
                submissionRepository.findByStatusInAndWithdrawalDeadlineAfter(
                        List.of("PENDING"), now.minusDays(1));
        
        for (PartyMaterialSubmission submission : submissionsNearDeadline) {
            if (submission.getWithdrawalDeadline() != null) {
                long hoursUntilDeadline = ChronoUnit.HOURS.between(now, submission.getWithdrawalDeadline());
                if (hoursUntilDeadline > 0 && hoursUntilDeadline <= 48) {
                    StudentProfile student = studentProfileRepository.findById(submission.getStudentId()).orElse(null);
                    if (student != null) {
                        tasks.add(new ReminderTask(
                                submission.getStudentId(),
                                student.getName(),
                                "WITHDRAW_DEADLINE",
                                submission.getStageName(),
                                "您的材料提交撤回期限即将到期（" + hoursUntilDeadline + "小时后），如需撤回请尽快操作",
                                now
                        ));
                    }
                }
            }
        }
    }

    public record ReminderTask(
            Long studentId,
            String studentName,
            String type,
            String stageName,
            String message,
            LocalDateTime triggerTime
    ) {}
}
