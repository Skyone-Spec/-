package edu.ruc.platform.party.service;

import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;

import java.util.List;

public interface PartyProgressApplicationService {

    PartyProgressResponse getProgress(Long studentId);

    PartyStageTimelineResponse getTimeline(Long studentId);

    List<ReminderResponse> listReminders(Long studentId);
}
