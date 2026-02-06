package com.dnd.moyeolak.domain.schedule.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.SchedulePollRepository;
import com.dnd.moyeolak.domain.schedule.service.ScheduleVoteService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleVoteServiceImpl implements ScheduleVoteService {

    private final SchedulePollRepository schedulePollRepository;

    @Override
    @Transactional
    public List<ScheduleVote> createVotes(Meeting meeting, Participant participant, List<LocalDateTime> availableSchedules) {
        SchedulePoll schedulePoll = schedulePollRepository.findByMeeting(meeting)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND));

        return availableSchedules.stream()
                .map(schedule -> ScheduleVote.of(participant, schedulePoll, schedule))
                .toList();
    }
}