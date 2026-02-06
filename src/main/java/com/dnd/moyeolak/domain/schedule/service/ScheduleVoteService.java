package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleVoteService {

    List<ScheduleVote> createVotes(Meeting meeting, Participant participant, List<LocalDateTime> availableSchedules);
}