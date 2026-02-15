package com.dnd.moyeolak.domain.participant.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
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

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;

    @Override
    public Participant getById(Long participantId) {
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }

    @Override
    public GetParticipantResponse getParticipant(Long participantId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPANT_NOT_FOUND));
        return GetParticipantResponse.from(participant);
    }

    @Override
    public ListParticipantResponse listParticipants(String meetingId) {
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
        return ListParticipantResponse.from(meeting.getParticipants());
    }

    @Override
    @Transactional
    public void save(Participant participant) {
        participantRepository.save(participant);
    }

    @Override
    public void validateLocalStorageKeyUnique(Meeting meeting, String localStorageKey) {
        if (participantRepository.existsByMeetingAndLocalStorageKey(meeting, localStorageKey)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
        }
    }
}
