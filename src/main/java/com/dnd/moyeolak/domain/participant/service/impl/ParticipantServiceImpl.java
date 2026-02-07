package com.dnd.moyeolak.domain.participant.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public void save(Participant participant) {
        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public Participant create(Meeting meeting, String name, String localStorageKey) {
        Participant participant = Participant.of(meeting, localStorageKey, name);
        meeting.addParticipant(participant);
        return participantRepository.save(participant);
    }

    @Override
    public void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey) {
        if (participantRepository.existsByMeetingAndLocalStorageKey(meeting, localStorageKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
        }
    }
}