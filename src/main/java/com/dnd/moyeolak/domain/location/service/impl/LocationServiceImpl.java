package com.dnd.moyeolak.domain.location.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final ParticipantService participantService;
    private final LocationPollRepository locationPollRepository;
    private final LocationVoteRepository locationVoteRepository;

    @Override
    @Transactional
    public void createLocationVote(CreateLocationVoteRequest request) {
        // 현재 추가 플로우가 실 참여자가 추가하는 것인지 수동으로 추가하는 로직인지 확인 후 분기
        LocationVote locationVote = LocationVote.fromByCreateLocationVoteRequest(request);
        if (StringUtil.isNullOrEmpty(request.localStorageKey())) {
            // 수동 추가 로직 (LocationVote 만 추가하면 됨)
            locationVoteRepository.save(locationVote);
        } else {
            // 실 참여자 추가 로직 (Participant 및 LocationVote 추가 필요)
            Participant participant = Participant.of(
                    Meeting.ofId(request.meetingId()), request.localStorageKey(), request.participantName(), locationVote
            );
            locationVote.assignParticipant(participant);
            participantService.save(participant);
        }
    }

}
