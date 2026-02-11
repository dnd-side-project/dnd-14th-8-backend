package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;

public interface ScheduleService {

    void createParticipantVote(String meetingId, CreateScheduleVoteRequest request);

    void updateParticipantVote(Long scheduleVoteId, UpdateScheduleVoteRequest request);
}
