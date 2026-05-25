package edu.ruc.platform.party.service;

import edu.ruc.platform.party.service.PartyReminderScheduler.ReminderTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PartyReminderNotificationService {

    private final Map<String, List<ReminderRecord>> reminderHistory = new ConcurrentHashMap<>();

    public void sendReminder(ReminderTask task) {
        String key = task.studentId() + ":" + task.type();
        reminderHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(
                new ReminderRecord(task.studentId(), task.studentName(), task.type(),
                        task.stageName(), task.message(), task.triggerTime(), "SENT")
        );

        log.info("[党团提醒] 学生: {}({}), 类型: {}, 阶段: {}, 消息: {}",
                task.studentName(), task.studentId(), task.type(),
                task.stageName(), task.message());

        if ("MATERIAL_PENDING".equals(task.type())) {
            log.info("[站内消息] 已发送给学生 {}: {}", task.studentName(), task.message());
        } else if ("STAGE_DEADLINE".equals(task.type())) {
            log.info("[邮件通知] 已发送给学生 {}: {}", task.studentName(), task.message());
        }
    }

    public List<ReminderRecord> getReminderHistory(Long studentId) {
        return reminderHistory.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(studentId + ":"))
                .flatMap(entry -> entry.getValue().stream())
                .toList();
    }

    public record ReminderRecord(
            Long studentId,
            String studentName,
            String type,
            String stageName,
            String message,
            java.time.LocalDateTime sentAt,
            String status
    ) {}
}
