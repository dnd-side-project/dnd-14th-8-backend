package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;

import java.util.List;

public interface ScheduleVoteService {

    Long createParticipantVote(String meetingId, CreateScheduleVoteRequest request);

    void updateParticipantVote(Long scheduleVoteId, UpdateScheduleVoteRequest request);

    List<ScheduleVote> findAllBySchedulePollId(Long schedulePollId);
}
