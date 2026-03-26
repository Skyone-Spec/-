package edu.ruc.platform.party.service;

import edu.ruc.platform.common.mock.MockDataStore;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPartyProgressService implements PartyProgressApplicationService {

    private final MockDataStore mockDataStore;

    @Override
    public PartyProgressResponse getProgress(Long studentId) {
        PartyProgressResponse progress = mockDataStore.partyProgress();
        return new PartyProgressResponse(
                progress.currentStage(),
                progress.stageStartDate(),
                (int) ChronoUnit.DAYS.between(progress.stageStartDate(), LocalDate.now()),
                progress.nextDeadline(),
                progress.completedActions(),
                progress.nextAction(),
                progress.nextActionRule()
        );
    }

    @Override
    public PartyStageTimelineResponse getTimeline(Long studentId) {
        return mockDataStore.partyTimeline();
    }

    @Override
    public List<ReminderResponse> listReminders(Long studentId) {
        LocalDate today = LocalDate.now();
        return mockDataStore.reminders().stream()
                .map(item -> new ReminderResponse(
                        item.title(),
                        item.content(),
                        item.remindDate(),
                        item.level(),
                        item.stageName(),
                        item.triggerRule(),
                        (int) ChronoUnit.DAYS.between(today, item.remindDate()),
                        item.remindDate().isBefore(today)))
                .toList();
    }
}
