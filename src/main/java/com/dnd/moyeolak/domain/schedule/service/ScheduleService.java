package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;

public interface ScheduleService {

    void updateParticipantVote(Long scheduleVoteId, UpdateScheduleVoteRequest request);
}
