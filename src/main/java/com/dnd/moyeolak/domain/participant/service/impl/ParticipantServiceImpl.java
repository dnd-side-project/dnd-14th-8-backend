package com.dnd.moyeolak.domain.participant.service.impl;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantServiceImpl implements ParticipantService {

    private final MeetingService meetingService;
    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public CreateParticipantResponse createWithSchedule(String meetingId, CreateParticipantWithScheduleRequest request) {
        Meeting meeting = meetingService.get(meetingId);
        validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        SchedulePoll schedulePoll = meeting.getSchedulePoll();
        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        Participant participant = Participant.of(meeting, request.localStorageKey(), request.name());

        List<ScheduleVote> votes = request.availableSchedules().stream()
                .map(schedule -> ScheduleVote.of(participant, schedulePoll, schedule))
                .toList();
        participant.getScheduleVotes().addAll(votes);

        meeting.addParticipant(participant);

        return CreateParticipantResponse.fromSchedule(participant, votes.size());
    }

    @Override
    @Transactional
    public CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request) {
        Meeting meeting = meetingService.get(meetingId);
        validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        LocationPoll locationPoll = meeting.getLocationPoll();
        if (locationPoll == null) {
            throw new BusinessException(ErrorCode.LOCATION_POLL_NOT_FOUND);
        }

        LocationVote vote = LocationVote.of(
                locationPoll,
                (Participant) null,
                request.location().address(),
                request.location().latitude(),
                request.location().longitude()
        );

        Participant participant = Participant.of(meeting, request.localStorageKey(), request.name(), vote);

        meeting.addParticipant(participant);

        return CreateParticipantResponse.fromLocation(participant);
    }

    @Override
    public GetParticipantResponse getParticipant(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
        return GetParticipantResponse.from(participant);
    }

    @Override
    public ListParticipantResponse listParticipants(String meetingId) {
        Meeting meeting = meetingService.get(meetingId);
        return ListParticipantResponse.from(meeting.getParticipants());
    }

    @Override
    @Transactional
    public void save(Participant participant) {
        participantRepository.save(participant);
    }

    private void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey) {
        if (participantRepository.existsByMeetingAndLocalStorageKey(meeting, localStorageKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
        }
    }
}
