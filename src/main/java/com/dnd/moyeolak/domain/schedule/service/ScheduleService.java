package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVotesRequest;

public interface ScheduleService {

    void updateScheduleVotes(Long scheduleVoteId, UpdateScheduleVotesRequest request);
}
