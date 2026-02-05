package com.dnd.moyeolak.domain.participant.service.impl;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.SchedulePollRepository;
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
public class ParticipantServiceImpl implements ParticipantService {

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final SchedulePollRepository schedulePollRepository;
    private final LocationPollRepository locationPollRepository;

    @Override
    @Transactional
    public CreateParticipantResponse createWithSchedule(String meetingId, CreateParticipantWithScheduleRequest request) {
        Meeting meeting = findMeetingById(meetingId);

        validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        SchedulePoll schedulePoll = schedulePollRepository.findByMeeting(meeting)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND));

        Participant participant = Participant.of(meeting, request.localStorageKey(), request.name());
        meeting.addParticipant(participant);

        List<ScheduleVote> scheduleVotes = createScheduleVotes(participant, schedulePoll, request.availableSchedules());
        participant.getScheduleVotes().addAll(scheduleVotes);

        participantRepository.save(participant);

        return CreateParticipantResponse.fromSchedule(participant, scheduleVotes.size());
    }

    @Override
    @Transactional
    public CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request) {
        Meeting meeting = findMeetingById(meetingId);

        validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        LocationPoll locationPoll = locationPollRepository.findByMeeting(meeting)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_POLL_NOT_FOUND));

        Participant participant = Participant.of(meeting, request.localStorageKey(), request.name());
        meeting.addParticipant(participant);

        LocationVote locationVote = LocationVote.of(
                locationPoll,
                participant,
                request.location().address(),
                request.location().latitude(),
                request.location().longitude()
        );
        participant.getLocationVotes().add(locationVote);

        participantRepository.save(participant);

        return CreateParticipantResponse.fromLocation(participant);
    }

    private Meeting findMeetingById(String meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
    }

    private void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey) {
        if (participantRepository.existsByMeetingAndLocalStorageKey(meeting, localStorageKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
        }
    }

    private List<ScheduleVote> createScheduleVotes(Participant participant, SchedulePoll schedulePoll,
                                                   List<LocalDateTime> availableSchedules) {
        return availableSchedules.stream()
                .map(schedule -> ScheduleVote.of(participant, schedulePoll, schedule))
                .toList();
    }
}
