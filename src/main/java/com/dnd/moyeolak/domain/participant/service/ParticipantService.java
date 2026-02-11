package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;

public interface ParticipantService {

    CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request);

    Participant getById(Long participantId);

    GetParticipantResponse getParticipant(Long participantId);

    ListParticipantResponse listParticipants(String meetingId);

    void save(Participant participant);

    void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey);
}
