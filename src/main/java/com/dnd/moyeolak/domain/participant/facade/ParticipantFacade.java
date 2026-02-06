package com.dnd.moyeolak.domain.participant.facade;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.service.LocationVoteService;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.service.ScheduleVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantFacade {

    private final MeetingService meetingService;
    private final ParticipantService participantService;
    private final ScheduleVoteService scheduleVoteService;
    private final LocationVoteService locationVoteService;

    @Transactional
    public CreateParticipantResponse createWithSchedule(String meetingId, CreateParticipantWithScheduleRequest request) {
        Meeting meeting = meetingService.get(meetingId);

        participantService.validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        Participant participant = participantService.create(meeting, request.name(), request.localStorageKey());

        List<ScheduleVote> scheduleVotes = scheduleVoteService.createVotes(
                meeting,
                participant,
                request.availableSchedules()
        );
        participant.getScheduleVotes().addAll(scheduleVotes);

        return CreateParticipantResponse.fromSchedule(participant, scheduleVotes.size());
    }

    @Transactional
    public CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request) {
        Meeting meeting = meetingService.get(meetingId);

        participantService.validateLocalStorageKeyUnique(meeting, request.localStorageKey());

        Participant participant = participantService.create(meeting, request.name(), request.localStorageKey());

        LocationVote locationVote = locationVoteService.createVote(
                meeting,
                participant,
                request.location().address(),
                request.location().latitude(),
                request.location().longitude()
        );
        participant.getLocationVotes().add(locationVote);

        return CreateParticipantResponse.fromLocation(participant);
    }
}