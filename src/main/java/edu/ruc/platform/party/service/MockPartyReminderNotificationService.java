package edu.ruc.platform.party.service;

import edu.ruc.platform.party.service.PartyReminderScheduler.ReminderTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPartyReminderNotificationService {

    public void sendReminder(ReminderTask task) {
        log.info("[Mock党团提醒] 学生: {}({}), 类型: {}, 消息: {}",
                task.studentName(), task.studentId(), task.type(), task.message());
    }
}
