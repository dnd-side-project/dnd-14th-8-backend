package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;

public interface ParticipantService {

    Participant create(Meeting meeting, String name, String localStorageKey);

    void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey);
}