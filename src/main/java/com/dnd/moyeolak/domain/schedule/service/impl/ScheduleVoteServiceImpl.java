package com.dnd.moyeolak.domain.schedule.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.SchedulePollRepository;
import com.dnd.moyeolak.domain.schedule.repository.ScheduleVoteRepository;
import com.dnd.moyeolak.domain.schedule.service.ScheduleVoteService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleVoteServiceImpl implements ScheduleVoteService {

    private final MeetingRepository meetingRepository;
    private final ParticipantService participantService;
    private final ScheduleVoteRepository scheduleVoteRepository;

    @Override
    @Transactional
    public Long createParticipantVote(String meetingId, CreateScheduleVoteRequest request) {
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        participantService.validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        SchedulePoll schedulePoll = meeting.getSchedulePoll();
        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        ScheduleVote scheduleVote = ScheduleVote.of(schedulePoll, request.votedDates());
        Participant participant = Participant.of(
                Meeting.ofId(meetingId), request.localStorageKey(), request.participantName(), scheduleVote
        );
        participantService.save(participant);

        return scheduleVote.getId();
    }

    @Override
    @Transactional
    public void updateParticipantVote(Long scheduleVoteId, UpdateScheduleVoteRequest request) {
        Participant participant = participantService.getById(request.participantId());
        participant.updateName(request.participantName());

        ScheduleVote scheduleVote = scheduleVoteRepository.findById(scheduleVoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_VOTE_NOT_FOUND));

        scheduleVote.updateDateTimeOption(request.votedDates());
    }

    @Override
    public List<ScheduleVote> findAllBySchedulePollId(Long schedulePollId) {
        return scheduleVoteRepository.findAllBySchedulePollId(schedulePollId);
    }

    @Override
    @Transactional
    public void deleteOutOfRangeVotes(SchedulePoll schedulePoll) {
        List<LocalDate> dateOptions = schedulePoll.getDateOptions();
        int startTime = schedulePoll.getStartTime();
        int endTime = schedulePoll.getEndTime();
        List<ScheduleVote> scheduleVotes = schedulePoll.getScheduleVotes();

        scheduleVotes.forEach(scheduleVote -> {
            List<LocalDateTime> votedDates = scheduleVote.getVotedDate();
            votedDates.removeIf(votedDate -> {
                if(!dateOptions.contains(votedDate.toLocalDate())) {
                    return true;
                }

                int minuteOfDay = votedDate.getHour() * 60 + votedDate.getMinute();
                return minuteOfDay < startTime || minuteOfDay >= endTime;
            });
        });
    }
}
