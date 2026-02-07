package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.entity.Participant;

public interface ParticipantService {

    CreateParticipantResponse createWithSchedule(String meetingId, CreateParticipantWithScheduleRequest request);

    CreateParticipantResponse createWithLocation(String meetingId, CreateParticipantWithLocationRequest request);

    void save(Participant participant);
}
