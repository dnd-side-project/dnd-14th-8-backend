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
import java.util.Optional;

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

        SchedulePoll schedulePoll = meeting.getSchedulePoll();
        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        Optional<Participant> existingParticipant = meeting.getParticipants().stream()
                .filter(p -> request.localStorageKey().equals(p.getLocalStorageKey()))
                .findFirst();

        if (existingParticipant.isPresent()) {
            Participant host = existingParticipant.get();
            if (!host.isHost() || !host.getScheduleVotes().isEmpty()) {
                throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
            }
            host.updateName(request.participantName());
            ScheduleVote scheduleVote = ScheduleVote.of(schedulePoll, request.votedDates());
            host.addScheduleVote(scheduleVote);
            scheduleVoteRepository.save(scheduleVote);
            return scheduleVote.getId();
        }

        ScheduleVote scheduleVote = ScheduleVote.of(schedulePoll, request.votedDates());
        Participant participant = Participant.of(
                meeting, request.localStorageKey(), request.participantName(), scheduleVote
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

        boolean crossesMidnight = startTime > endTime;

        scheduleVotes.forEach(scheduleVote -> {
            List<LocalDateTime> votedDates = scheduleVote.getVotedDate();
            votedDates.removeIf(votedDate -> {
                if(!dateOptions.contains(votedDate.toLocalDate())) {
                    return true;
                }

                int minuteOfDay = votedDate.getHour() * 60 + votedDate.getMinute();
                if (crossesMidnight) {
                    return minuteOfDay >= endTime && minuteOfDay < startTime;
                }
                return minuteOfDay < startTime || minuteOfDay >= endTime;
            });
        });

        scheduleVotes.removeIf(vote -> vote.getVotedDate().isEmpty());
    }
}
