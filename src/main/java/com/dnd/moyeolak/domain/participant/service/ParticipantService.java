package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;

public interface ParticipantService {

    Participant getById(Long participantId);

    ParticipantResponse findByMeetingIdAndLocalStorageKey(String meetingId, String localStorageKey);

    ListParticipantResponse listParticipants(String meetingId);

    void save(Participant participant);

    void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey);
}
