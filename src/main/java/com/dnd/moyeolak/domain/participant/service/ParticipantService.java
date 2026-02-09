package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;

public interface ParticipantService {

    CreateParticipantResponse createWithSchedule(String meetingId, CreateParticipantWithScheduleRequest request);

    CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request);

    Participant getById(Long participantId);

    GetParticipantResponse getParticipant(Long participantId);

    ListParticipantResponse listParticipants(String meetingId);

    void save(Participant participant);
}
