package com.dnd.moyeolak.domain.schedule.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.ScheduleVoteRepository;
import com.dnd.moyeolak.domain.schedule.service.ScheduleService;
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
public class ScheduleServiceImpl implements ScheduleService {

    private final MeetingService meetingService;
    private final ParticipantService participantService;
    private final ScheduleVoteRepository scheduleVoteRepository;

    @Override
    @Transactional
    public void createParticipantVote(String meetingId, CreateScheduleVoteRequest request) {
        Meeting meeting = meetingService.get(meetingId);

        participantService.validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        SchedulePoll schedulePoll = meeting.getSchedulePoll();
        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        ScheduleVote scheduleVote = ScheduleVote.of(schedulePoll, request.votedDates());
        Participant participant = Participant.of(
                Meeting.ofId(meetingId), request.localStorageKey(), request.participantName(), scheduleVote
        );
        meeting.addParticipant(participant);
    }

    @Override
    @Transactional
    public void updateParticipantVote(Long scheduleVoteId, UpdateScheduleVoteRequest request) {
        Participant participant = participantService.getById(request.participantId());
        participant.updateName(request.participantName());

        ScheduleVote scheduleVote = scheduleVoteRepository.findById(scheduleVoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_VOTE_NOT_FOUND));

        if (request.isSelectingAvailable()) {
            scheduleVote.updateDateTimeOption(request.votedDates());
        } else {
            List<LocalDateTime> allSlots = scheduleVote.getSchedulePoll().generateAllTimeSlots();
            List<LocalDateTime> availableSlots = allSlots.stream()
                    .filter(dateTime -> !request.votedDates().contains(dateTime))
                    .toList();
            scheduleVote.updateDateTimeOption(availableSlots);
        }
    }
}
