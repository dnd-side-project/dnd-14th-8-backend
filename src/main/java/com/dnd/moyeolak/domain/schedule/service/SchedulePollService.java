package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;

public interface SchedulePollService {

    void updateSchedulePoll(String meetingId, UpdateSchedulePollRequest request);

    void confirmSchedulePoll(Long schedulePollId);
}
