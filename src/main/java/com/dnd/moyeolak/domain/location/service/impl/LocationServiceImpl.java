package com.dnd.moyeolak.domain.location.service.impl;

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
import org.springframework.util.StringUtils;

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
        LocationVote locationVote = LocationVote.fromByCreateLocationVoteRequest(request);
        if (StringUtils.hasText(request.localStorageKey())) {
            Participant participant = Participant.of(
                    Meeting.ofId(request.meetingId()), request.localStorageKey(), request.participantName(), locationVote
            );
            participantService.save(participant);
        } else {
            locationVoteRepository.save(locationVote);
        }
    }

    @Override
    @Transactional
    public void deleteLocationVote(Long locationVoteId) {
        locationVoteRepository.deleteById(locationVoteId);
    }
}
